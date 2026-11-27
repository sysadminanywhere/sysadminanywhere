"""Local, read-only directory service fixture for the browser smoke test."""

import base64
import hashlib
import hmac
import json
import os
import time
from datetime import date, timedelta
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlsplit
from uuid import NAMESPACE_DNS, uuid5


def encode(value):
    return base64.urlsafe_b64encode(value).rstrip(b"=")


def organization_fixture():
    def dn(name):
        return f"CN={name},OU=Smoke,DC=example,DC=test"

    director = "Smoke Director"
    operator = "Smoke Operator"
    assignments = [
        (director, "PL", "Warsaw", "Director", None),
        (operator, "DE", "Berlin", "Operations Manager", director),
        ("Smoke Specialist", "DE", "Berlin", "Support Specialist", operator),
        ("Smoke Analyst", "PL", "Warsaw", "Analyst", director),
    ]
    return [
        {"id": str(uuid5(NAMESPACE_DNS, dn(name))), "distinguishedName": dn(name),
         "cn": name, "name": name, "login": name.lower().replace(" ", "-"),
         "title": title, "department": "Operations", "country": country, "office": office,
         "managerDn": dn(manager) if manager else None, "disabled": False}
        for name, country, office, title, manager in assignments
    ]


class Directory(BaseHTTPRequestHandler):
    def respond(self, body, status=200):
        data = json.dumps(body).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def read_body(self):
        if self.headers.get("Transfer-Encoding") == "chunked":
            data = bytearray()
            while size := int(self.rfile.readline().split(b";", 1)[0], 16):
                data.extend(self.rfile.read(size))
                self.rfile.readline()
            self.rfile.readline()
            return data
        return self.rfile.read(int(self.headers.get("Content-Length", 0)))

    def do_POST(self):
        if self.path == "/api/ldap/search":
            self.respond([])
            return
        if self.path == "/api/ldap/count":
            self.respond(0)
            return
        request = json.loads(self.read_body())
        if self.path == "/api/wmi/execute":
            patches = [{"HotFixID": "KB0000001", "InstalledOn": str(date.today() - timedelta(days=1))}]
            query = request.get("wqlQuery", "")
            if "Win32_QuickFixEngineering" in query:
                self.respond(patches)
            elif "Win32_Processor" in query:
                self.respond([{"Name": "Smoke Processor"}])
            else:
                self.respond([])
            return
        if self.path != "/api/ldap/authenticate":
            self.respond([], 404)
            return
        username = os.environ["E2E_USERNAME"]
        if request.get("username") != username or request.get("password") != os.environ["E2E_PASSWORD"]:
            self.respond({}, 401)
            return
        roles = ["ROLE_ADMIN"]
        claims = {"sub": username, "roles": roles, "service": request.get("service", "main"),
                  "exp": int(time.time()) + 3600}
        unsigned = encode(b'{"alg":"HS256"}') + b"." + encode(json.dumps(claims).encode())
        signature = encode(hmac.digest(os.environ["JWT_SECRET"].encode(), unsigned, hashlib.sha256))
        self.respond({"username": username, "roles": roles, "token": (unsigned + b"." + signature).decode()})

    def do_GET(self):
        route = urlsplit(self.path)
        if route.path == "/ping":
            self.respond("pong")
        elif route.path == "/api/users/organization/snapshot":
            self.respond(organization_fixture())
        elif route.path == "/api/users/list":
            search_name = os.environ.get("E2E_SEARCH_NAME", "Smoke Operator")
            if any(search_name in value for values in parse_qs(route.query).values() for value in values):
                self.respond([{"cn": search_name, "displayName": "Fixture user", "sAMAccountName": "smoke-user"}])
            else:
                self.respond([])
        elif route.path == "/api/computers/list" and not parse_qs(route.query).get("filters"):
            self.respond([{"cn": "Smoke Workstation"}])
        elif route.path.endswith("/list"):
            self.respond([])
        elif route.path in {"/api/users", "/api/computers", "/api/contacts", "/api/groups", "/api/printers"}:
            self.respond({"content": [], "page": 0, "size": 10, "totalElements": 0, "totalPages": 0})
        else:
            self.respond({}, 404)


if __name__ == "__main__":
    ThreadingHTTPServer(("127.0.0.1", int(os.environ.get("E2E_DIRECTORY_PORT", "8081"))), Directory).serve_forever()
