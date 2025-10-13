class Vo {
  constructor() {
    this.eventBuffer = [], this.handledTypes = [], this.copilotMain = null, this.debug = !1, this.eventProxy = {
      functionCallQueue: [],
      dispatchEvent(...t) {
        return this.functionCallQueue.push({ name: "dispatchEvent", args: t }), !0;
      },
      removeEventListener(...t) {
        this.functionCallQueue.push({ name: "removeEventListener", args: t });
      },
      addEventListener(...t) {
        this.functionCallQueue.push({ name: "addEventListener", args: t });
      },
      processQueue(t) {
        this.functionCallQueue.forEach((n) => {
          t[n.name].call(t, ...n.args);
        }), this.functionCallQueue = [];
      }
    };
  }
  getEventTarget() {
    return this.copilotMain ? this.copilotMain : (this.copilotMain = document.querySelector("copilot-main"), this.copilotMain ? (this.eventProxy.processQueue(this.copilotMain), this.copilotMain) : this.eventProxy);
  }
  on(t, n) {
    const r = n;
    return this.getEventTarget().addEventListener(t, r), this.handledTypes.push(t), this.flush(t), () => this.off(t, r);
  }
  once(t, n) {
    this.getEventTarget().addEventListener(t, n, { once: !0 });
  }
  off(t, n) {
    this.getEventTarget().removeEventListener(t, n);
    const r = this.handledTypes.indexOf(t, 0);
    r > -1 && this.handledTypes.splice(r, 1);
  }
  emit(t, n) {
    const r = new CustomEvent(t, { detail: n, cancelable: !0 });
    return this.handledTypes.includes(t) || this.eventBuffer.push(r), this.debug && console.debug("Emit event", r), this.getEventTarget().dispatchEvent(r), r.defaultPrevented;
  }
  emitUnsafe({ type: t, data: n }) {
    return this.emit(t, n);
  }
  // Communication with server via eventbus
  send(t, n) {
    const r = new CustomEvent("copilot-send", { detail: { command: t, data: n } });
    this.getEventTarget().dispatchEvent(r);
  }
  // Listeners for Copilot itself
  onSend(t) {
    this.on("copilot-send", t);
  }
  offSend(t) {
    this.off("copilot-send", t);
  }
  flush(t) {
    const n = [];
    this.eventBuffer.filter((r) => r.type === t).forEach((r) => {
      this.getEventTarget().dispatchEvent(r), n.push(r);
    }), this.eventBuffer = this.eventBuffer.filter((r) => !n.includes(r));
  }
}
var Ro = {
  0: "Invalid value for configuration 'enforceActions', expected 'never', 'always' or 'observed'",
  1: function(t, n) {
    return "Cannot apply '" + t + "' to '" + n.toString() + "': Field not found.";
  },
  /*
  2(prop) {
      return `invalid decorator for '${prop.toString()}'`
  },
  3(prop) {
      return `Cannot decorate '${prop.toString()}': action can only be used on properties with a function value.`
  },
  4(prop) {
      return `Cannot decorate '${prop.toString()}': computed can only be used on getter properties.`
  },
  */
  5: "'keys()' can only be used on observable objects, arrays, sets and maps",
  6: "'values()' can only be used on observable objects, arrays, sets and maps",
  7: "'entries()' can only be used on observable objects, arrays and maps",
  8: "'set()' can only be used on observable objects, arrays and maps",
  9: "'remove()' can only be used on observable objects, arrays and maps",
  10: "'has()' can only be used on observable objects, arrays and maps",
  11: "'get()' can only be used on observable objects, arrays and maps",
  12: "Invalid annotation",
  13: "Dynamic observable objects cannot be frozen. If you're passing observables to 3rd party component/function that calls Object.freeze, pass copy instead: toJS(observable)",
  14: "Intercept handlers should return nothing or a change object",
  15: "Observable arrays cannot be frozen. If you're passing observables to 3rd party component/function that calls Object.freeze, pass copy instead: toJS(observable)",
  16: "Modification exception: the internal structure of an observable array was changed.",
  17: function(t, n) {
    return "[mobx.array] Index out of bounds, " + t + " is larger than " + n;
  },
  18: "mobx.map requires Map polyfill for the current browser. Check babel-polyfill or core-js/es6/map.js",
  19: function(t) {
    return "Cannot initialize from classes that inherit from Map: " + t.constructor.name;
  },
  20: function(t) {
    return "Cannot initialize map from " + t;
  },
  21: function(t) {
    return "Cannot convert to map from '" + t + "'";
  },
  22: "mobx.set requires Set polyfill for the current browser. Check babel-polyfill or core-js/es6/set.js",
  23: "It is not possible to get index atoms from arrays",
  24: function(t) {
    return "Cannot obtain administration from " + t;
  },
  25: function(t, n) {
    return "the entry '" + t + "' does not exist in the observable map '" + n + "'";
  },
  26: "please specify a property",
  27: function(t, n) {
    return "no observable property '" + t.toString() + "' found on the observable object '" + n + "'";
  },
  28: function(t) {
    return "Cannot obtain atom from " + t;
  },
  29: "Expecting some object",
  30: "invalid action stack. did you forget to finish an action?",
  31: "missing option for computed: get",
  32: function(t, n) {
    return "Cycle detected in computation " + t + ": " + n;
  },
  33: function(t) {
    return "The setter of computed value '" + t + "' is trying to update itself. Did you intend to update an _observable_ value, instead of the computed property?";
  },
  34: function(t) {
    return "[ComputedValue '" + t + "'] It is not possible to assign a new value to a computed value.";
  },
  35: "There are multiple, different versions of MobX active. Make sure MobX is loaded only once or use `configure({ isolateGlobalState: true })`",
  36: "isolateGlobalState should be called before MobX is running any reactions",
  37: function(t) {
    return "[mobx] `observableArray." + t + "()` mutates the array in-place, which is not allowed inside a derivation. Use `array.slice()." + t + "()` instead";
  },
  38: "'ownKeys()' can only be used on observable objects",
  39: "'defineProperty()' can only be used on observable objects"
}, jo = process.env.NODE_ENV !== "production" ? Ro : {};
function h(e) {
  for (var t = arguments.length, n = new Array(t > 1 ? t - 1 : 0), r = 1; r < t; r++)
    n[r - 1] = arguments[r];
  if (process.env.NODE_ENV !== "production") {
    var i = typeof e == "string" ? e : jo[e];
    throw typeof i == "function" && (i = i.apply(null, n)), new Error("[MobX] " + i);
  }
  throw new Error(typeof e == "number" ? "[MobX] minified error nr: " + e + (n.length ? " " + n.map(String).join(",") : "") + ". Find the full error at: https://github.com/mobxjs/mobx/blob/main/packages/mobx/src/errors.ts" : "[MobX] " + e);
}
var Mo = {};
function Kn() {
  return typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : typeof global < "u" ? global : typeof self < "u" ? self : Mo;
}
var ii = Object.assign, Ut = Object.getOwnPropertyDescriptor, Z = Object.defineProperty, tn = Object.prototype, Bt = [];
Object.freeze(Bt);
var qn = {};
Object.freeze(qn);
var Lo = typeof Proxy < "u", zo = /* @__PURE__ */ Object.toString();
function oi() {
  Lo || h(process.env.NODE_ENV !== "production" ? "`Proxy` objects are not available in the current environment. Please configure MobX to enable a fallback implementation.`" : "Proxy not available");
}
function tt(e) {
  process.env.NODE_ENV !== "production" && f.verifyProxies && h("MobX is currently configured to be able to run in ES5 mode, but in ES5 MobX won't be able to " + e);
}
function B() {
  return ++f.mobxGuid;
}
function Wn(e) {
  var t = !1;
  return function() {
    if (!t)
      return t = !0, e.apply(this, arguments);
  };
}
var Le = function() {
};
function A(e) {
  return typeof e == "function";
}
function xe(e) {
  var t = typeof e;
  switch (t) {
    case "string":
    case "symbol":
    case "number":
      return !0;
  }
  return !1;
}
function nn(e) {
  return e !== null && typeof e == "object";
}
function P(e) {
  if (!nn(e))
    return !1;
  var t = Object.getPrototypeOf(e);
  if (t == null)
    return !0;
  var n = Object.hasOwnProperty.call(t, "constructor") && t.constructor;
  return typeof n == "function" && n.toString() === zo;
}
function ai(e) {
  var t = e?.constructor;
  return t ? t.name === "GeneratorFunction" || t.displayName === "GeneratorFunction" : !1;
}
function rn(e, t, n) {
  Z(e, t, {
    enumerable: !1,
    writable: !0,
    configurable: !0,
    value: n
  });
}
function si(e, t, n) {
  Z(e, t, {
    enumerable: !1,
    writable: !1,
    configurable: !0,
    value: n
  });
}
function Te(e, t) {
  var n = "isMobX" + e;
  return t.prototype[n] = !0, function(r) {
    return nn(r) && r[n] === !0;
  };
}
function Ge(e) {
  return e != null && Object.prototype.toString.call(e) === "[object Map]";
}
function Uo(e) {
  var t = Object.getPrototypeOf(e), n = Object.getPrototypeOf(t), r = Object.getPrototypeOf(n);
  return r === null;
}
function re(e) {
  return e != null && Object.prototype.toString.call(e) === "[object Set]";
}
var li = typeof Object.getOwnPropertySymbols < "u";
function Bo(e) {
  var t = Object.keys(e);
  if (!li)
    return t;
  var n = Object.getOwnPropertySymbols(e);
  return n.length ? [].concat(t, n.filter(function(r) {
    return tn.propertyIsEnumerable.call(e, r);
  })) : t;
}
var pt = typeof Reflect < "u" && Reflect.ownKeys ? Reflect.ownKeys : li ? function(e) {
  return Object.getOwnPropertyNames(e).concat(Object.getOwnPropertySymbols(e));
} : (
  /* istanbul ignore next */
  Object.getOwnPropertyNames
);
function Pn(e) {
  return typeof e == "string" ? e : typeof e == "symbol" ? e.toString() : new String(e).toString();
}
function ci(e) {
  return e === null ? null : typeof e == "object" ? "" + e : e;
}
function H(e, t) {
  return tn.hasOwnProperty.call(e, t);
}
var Fo = Object.getOwnPropertyDescriptors || function(t) {
  var n = {};
  return pt(t).forEach(function(r) {
    n[r] = Ut(t, r);
  }), n;
};
function D(e, t) {
  return !!(e & t);
}
function T(e, t, n) {
  return n ? e |= t : e &= ~t, e;
}
function sr(e, t) {
  (t == null || t > e.length) && (t = e.length);
  for (var n = 0, r = Array(t); n < t; n++) r[n] = e[n];
  return r;
}
function Ho(e, t) {
  for (var n = 0; n < t.length; n++) {
    var r = t[n];
    r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), Object.defineProperty(e, qo(r.key), r);
  }
}
function Je(e, t, n) {
  return t && Ho(e.prototype, t), Object.defineProperty(e, "prototype", {
    writable: !1
  }), e;
}
function ze(e, t) {
  var n = typeof Symbol < "u" && e[Symbol.iterator] || e["@@iterator"];
  if (n) return (n = n.call(e)).next.bind(n);
  if (Array.isArray(e) || (n = Wo(e)) || t) {
    n && (e = n);
    var r = 0;
    return function() {
      return r >= e.length ? {
        done: !0
      } : {
        done: !1,
        value: e[r++]
      };
    };
  }
  throw new TypeError(`Invalid attempt to iterate non-iterable instance.
In order to be iterable, non-array objects must have a [Symbol.iterator]() method.`);
}
function he() {
  return he = Object.assign ? Object.assign.bind() : function(e) {
    for (var t = 1; t < arguments.length; t++) {
      var n = arguments[t];
      for (var r in n) ({}).hasOwnProperty.call(n, r) && (e[r] = n[r]);
    }
    return e;
  }, he.apply(null, arguments);
}
function ui(e, t) {
  e.prototype = Object.create(t.prototype), e.prototype.constructor = e, $n(e, t);
}
function $n(e, t) {
  return $n = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function(n, r) {
    return n.__proto__ = r, n;
  }, $n(e, t);
}
function Ko(e, t) {
  if (typeof e != "object" || !e) return e;
  var n = e[Symbol.toPrimitive];
  if (n !== void 0) {
    var r = n.call(e, t);
    if (typeof r != "object") return r;
    throw new TypeError("@@toPrimitive must return a primitive value.");
  }
  return String(e);
}
function qo(e) {
  var t = Ko(e, "string");
  return typeof t == "symbol" ? t : t + "";
}
function Wo(e, t) {
  if (e) {
    if (typeof e == "string") return sr(e, t);
    var n = {}.toString.call(e).slice(8, -1);
    return n === "Object" && e.constructor && (n = e.constructor.name), n === "Map" || n === "Set" ? Array.from(e) : n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n) ? sr(e, t) : void 0;
  }
}
var ie = /* @__PURE__ */ Symbol("mobx-stored-annotations");
function Q(e) {
  function t(n, r) {
    if (At(r))
      return e.decorate_20223_(n, r);
    Ot(n, r, e);
  }
  return Object.assign(t, e);
}
function Ot(e, t, n) {
  if (H(e, ie) || rn(e, ie, he({}, e[ie])), process.env.NODE_ENV !== "production" && Ft(n) && !H(e[ie], t)) {
    var r = e.constructor.name + ".prototype." + t.toString();
    h("'" + r + "' is decorated with 'override', but no such decorated member was found on prototype.");
  }
  Go(e, n, t), Ft(n) || (e[ie][t] = n);
}
function Go(e, t, n) {
  if (process.env.NODE_ENV !== "production" && !Ft(t) && H(e[ie], n)) {
    var r = e.constructor.name + ".prototype." + n.toString(), i = e[ie][n].annotationType_, o = t.annotationType_;
    h("Cannot apply '@" + o + "' to '" + r + "':" + (`
The field is already decorated with '@` + i + "'.") + `
Re-decorating fields is not allowed.
Use '@override' decorator for methods overridden by subclass.`);
  }
}
function At(e) {
  return typeof e == "object" && typeof e.kind == "string";
}
function on(e, t) {
  process.env.NODE_ENV !== "production" && !t.includes(e.kind) && h("The decorator applied to '" + String(e.name) + "' cannot be used on a " + e.kind + " element");
}
var m = /* @__PURE__ */ Symbol("mobx administration"), ge = /* @__PURE__ */ function() {
  function e(n) {
    n === void 0 && (n = process.env.NODE_ENV !== "production" ? "Atom@" + B() : "Atom"), this.name_ = void 0, this.flags_ = 0, this.observers_ = /* @__PURE__ */ new Set(), this.lastAccessedBy_ = 0, this.lowestObserverState_ = _.NOT_TRACKING_, this.onBOL = void 0, this.onBUOL = void 0, this.name_ = n;
  }
  var t = e.prototype;
  return t.onBO = function() {
    this.onBOL && this.onBOL.forEach(function(r) {
      return r();
    });
  }, t.onBUO = function() {
    this.onBUOL && this.onBUOL.forEach(function(r) {
      return r();
    });
  }, t.reportObserved = function() {
    return xi(this);
  }, t.reportChanged = function() {
    M(), Ni(this), L();
  }, t.toString = function() {
    return this.name_;
  }, Je(e, [{
    key: "isBeingObserved",
    get: function() {
      return D(this.flags_, e.isBeingObservedMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isBeingObservedMask_, r);
    }
  }, {
    key: "isPendingUnobservation",
    get: function() {
      return D(this.flags_, e.isPendingUnobservationMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isPendingUnobservationMask_, r);
    }
  }, {
    key: "diffValue",
    get: function() {
      return D(this.flags_, e.diffValueMask_) ? 1 : 0;
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.diffValueMask_, r === 1);
    }
  }]);
}();
ge.isBeingObservedMask_ = 1;
ge.isPendingUnobservationMask_ = 2;
ge.diffValueMask_ = 4;
var Gn = /* @__PURE__ */ Te("Atom", ge);
function di(e, t, n) {
  t === void 0 && (t = Le), n === void 0 && (n = Le);
  var r = new ge(e);
  return t !== Le && is(r, t), n !== Le && Ri(r, n), r;
}
function Jo(e, t) {
  return Gi(e, t);
}
function Xo(e, t) {
  return Object.is ? Object.is(e, t) : e === t ? e !== 0 || 1 / e === 1 / t : e !== e && t !== t;
}
var Be = {
  structural: Jo,
  default: Xo
};
function Ne(e, t, n) {
  return bt(e) ? e : Array.isArray(e) ? S.array(e, {
    name: n
  }) : P(e) ? S.object(e, void 0, {
    name: n
  }) : Ge(e) ? S.map(e, {
    name: n
  }) : re(e) ? S.set(e, {
    name: n
  }) : typeof e == "function" && !Fe(e) && !mt(e) ? ai(e) ? He(e) : gt(n, e) : e;
}
function Yo(e, t, n) {
  if (e == null || Ze(e) || fn(e) || me(e) || X(e))
    return e;
  if (Array.isArray(e))
    return S.array(e, {
      name: n,
      deep: !1
    });
  if (P(e))
    return S.object(e, void 0, {
      name: n,
      deep: !1
    });
  if (Ge(e))
    return S.map(e, {
      name: n,
      deep: !1
    });
  if (re(e))
    return S.set(e, {
      name: n,
      deep: !1
    });
  process.env.NODE_ENV !== "production" && h("The shallow modifier / decorator can only used in combination with arrays, objects, maps and sets");
}
function an(e) {
  return e;
}
function Zo(e, t) {
  return process.env.NODE_ENV !== "production" && bt(e) && h("observable.struct should not be used with observable values"), Gi(e, t) ? t : e;
}
var Qo = "override";
function Ft(e) {
  return e.annotationType_ === Qo;
}
function St(e, t) {
  return {
    annotationType_: e,
    options_: t,
    make_: ea,
    extend_: ta,
    decorate_20223_: na
  };
}
function ea(e, t, n, r) {
  var i;
  if ((i = this.options_) != null && i.bound)
    return this.extend_(e, t, n, !1) === null ? 0 : 1;
  if (r === e.target_)
    return this.extend_(e, t, n, !1) === null ? 0 : 2;
  if (Fe(n.value))
    return 1;
  var o = fi(e, this, t, n, !1);
  return Z(r, t, o), 2;
}
function ta(e, t, n, r) {
  var i = fi(e, this, t, n);
  return e.defineProperty_(t, i, r);
}
function na(e, t) {
  process.env.NODE_ENV !== "production" && on(t, ["method", "field"]);
  var n = t.kind, r = t.name, i = t.addInitializer, o = this, a = function(c) {
    var u, d, v, p;
    return Ce((u = (d = o.options_) == null ? void 0 : d.name) != null ? u : r.toString(), c, (v = (p = o.options_) == null ? void 0 : p.autoAction) != null ? v : !1);
  };
  if (n == "field")
    return function(l) {
      var c, u = l;
      return Fe(u) || (u = a(u)), (c = o.options_) != null && c.bound && (u = u.bind(this), u.isMobxAction = !0), u;
    };
  if (n == "method") {
    var s;
    return Fe(e) || (e = a(e)), (s = this.options_) != null && s.bound && i(function() {
      var l = this, c = l[r].bind(l);
      c.isMobxAction = !0, l[r] = c;
    }), e;
  }
  h("Cannot apply '" + o.annotationType_ + "' to '" + String(r) + "' (kind: " + n + "):" + (`
'` + o.annotationType_ + "' can only be used on properties with a function value."));
}
function ra(e, t, n, r) {
  var i = t.annotationType_, o = r.value;
  process.env.NODE_ENV !== "production" && !A(o) && h("Cannot apply '" + i + "' to '" + e.name_ + "." + n.toString() + "':" + (`
'` + i + "' can only be used on properties with a function value."));
}
function fi(e, t, n, r, i) {
  var o, a, s, l, c, u, d;
  i === void 0 && (i = f.safeDescriptors), ra(e, t, n, r);
  var v = r.value;
  if ((o = t.options_) != null && o.bound) {
    var p;
    v = v.bind((p = e.proxy_) != null ? p : e.target_);
  }
  return {
    value: Ce(
      (a = (s = t.options_) == null ? void 0 : s.name) != null ? a : n.toString(),
      v,
      (l = (c = t.options_) == null ? void 0 : c.autoAction) != null ? l : !1,
      // https://github.com/mobxjs/mobx/discussions/3140
      (u = t.options_) != null && u.bound ? (d = e.proxy_) != null ? d : e.target_ : void 0
    ),
    // Non-configurable for classes
    // prevents accidental field redefinition in subclass
    configurable: i ? e.isPlainObject_ : !0,
    // https://github.com/mobxjs/mobx/pull/2641#issuecomment-737292058
    enumerable: !1,
    // Non-obsevable, therefore non-writable
    // Also prevents rewriting in subclass constructor
    writable: !i
  };
}
function hi(e, t) {
  return {
    annotationType_: e,
    options_: t,
    make_: ia,
    extend_: oa,
    decorate_20223_: aa
  };
}
function ia(e, t, n, r) {
  var i;
  if (r === e.target_)
    return this.extend_(e, t, n, !1) === null ? 0 : 2;
  if ((i = this.options_) != null && i.bound && (!H(e.target_, t) || !mt(e.target_[t])) && this.extend_(e, t, n, !1) === null)
    return 0;
  if (mt(n.value))
    return 1;
  var o = vi(e, this, t, n, !1, !1);
  return Z(r, t, o), 2;
}
function oa(e, t, n, r) {
  var i, o = vi(e, this, t, n, (i = this.options_) == null ? void 0 : i.bound);
  return e.defineProperty_(t, o, r);
}
function aa(e, t) {
  var n;
  process.env.NODE_ENV !== "production" && on(t, ["method"]);
  var r = t.name, i = t.addInitializer;
  return mt(e) || (e = He(e)), (n = this.options_) != null && n.bound && i(function() {
    var o = this, a = o[r].bind(o);
    a.isMobXFlow = !0, o[r] = a;
  }), e;
}
function sa(e, t, n, r) {
  var i = t.annotationType_, o = r.value;
  process.env.NODE_ENV !== "production" && !A(o) && h("Cannot apply '" + i + "' to '" + e.name_ + "." + n.toString() + "':" + (`
'` + i + "' can only be used on properties with a generator function value."));
}
function vi(e, t, n, r, i, o) {
  o === void 0 && (o = f.safeDescriptors), sa(e, t, n, r);
  var a = r.value;
  if (mt(a) || (a = He(a)), i) {
    var s;
    a = a.bind((s = e.proxy_) != null ? s : e.target_), a.isMobXFlow = !0;
  }
  return {
    value: a,
    // Non-configurable for classes
    // prevents accidental field redefinition in subclass
    configurable: o ? e.isPlainObject_ : !0,
    // https://github.com/mobxjs/mobx/pull/2641#issuecomment-737292058
    enumerable: !1,
    // Non-obsevable, therefore non-writable
    // Also prevents rewriting in subclass constructor
    writable: !o
  };
}
function Jn(e, t) {
  return {
    annotationType_: e,
    options_: t,
    make_: la,
    extend_: ca,
    decorate_20223_: ua
  };
}
function la(e, t, n) {
  return this.extend_(e, t, n, !1) === null ? 0 : 1;
}
function ca(e, t, n, r) {
  return da(e, this, t, n), e.defineComputedProperty_(t, he({}, this.options_, {
    get: n.get,
    set: n.set
  }), r);
}
function ua(e, t) {
  process.env.NODE_ENV !== "production" && on(t, ["getter"]);
  var n = this, r = t.name, i = t.addInitializer;
  return i(function() {
    var o = Ye(this)[m], a = he({}, n.options_, {
      get: e,
      context: this
    });
    a.name || (a.name = process.env.NODE_ENV !== "production" ? o.name_ + "." + r.toString() : "ObservableObject." + r.toString()), o.values_.set(r, new U(a));
  }), function() {
    return this[m].getObservablePropValue_(r);
  };
}
function da(e, t, n, r) {
  var i = t.annotationType_, o = r.get;
  process.env.NODE_ENV !== "production" && !o && h("Cannot apply '" + i + "' to '" + e.name_ + "." + n.toString() + "':" + (`
'` + i + "' can only be used on getter(+setter) properties."));
}
function sn(e, t) {
  return {
    annotationType_: e,
    options_: t,
    make_: fa,
    extend_: ha,
    decorate_20223_: va
  };
}
function fa(e, t, n) {
  return this.extend_(e, t, n, !1) === null ? 0 : 1;
}
function ha(e, t, n, r) {
  var i, o;
  return pa(e, this, t, n), e.defineObservableProperty_(t, n.value, (i = (o = this.options_) == null ? void 0 : o.enhancer) != null ? i : Ne, r);
}
function va(e, t) {
  if (process.env.NODE_ENV !== "production") {
    if (t.kind === "field")
      throw h("Please use `@observable accessor " + String(t.name) + "` instead of `@observable " + String(t.name) + "`");
    on(t, ["accessor"]);
  }
  var n = this, r = t.kind, i = t.name, o = /* @__PURE__ */ new WeakSet();
  function a(s, l) {
    var c, u, d = Ye(s)[m], v = new Se(l, (c = (u = n.options_) == null ? void 0 : u.enhancer) != null ? c : Ne, process.env.NODE_ENV !== "production" ? d.name_ + "." + i.toString() : "ObservableObject." + i.toString(), !1);
    d.values_.set(i, v), o.add(s);
  }
  if (r == "accessor")
    return {
      get: function() {
        return o.has(this) || a(this, e.get.call(this)), this[m].getObservablePropValue_(i);
      },
      set: function(l) {
        return o.has(this) || a(this, l), this[m].setObservablePropValue_(i, l);
      },
      init: function(l) {
        return o.has(this) || a(this, l), l;
      }
    };
}
function pa(e, t, n, r) {
  var i = t.annotationType_;
  process.env.NODE_ENV !== "production" && !("value" in r) && h("Cannot apply '" + i + "' to '" + e.name_ + "." + n.toString() + "':" + (`
'` + i + "' cannot be used on getter/setter properties"));
}
var ga = "true", ma = /* @__PURE__ */ pi();
function pi(e) {
  return {
    annotationType_: ga,
    options_: e,
    make_: ba,
    extend_: _a,
    decorate_20223_: ya
  };
}
function ba(e, t, n, r) {
  var i, o;
  if (n.get)
    return ln.make_(e, t, n, r);
  if (n.set) {
    var a = Ce(t.toString(), n.set);
    return r === e.target_ ? e.defineProperty_(t, {
      configurable: f.safeDescriptors ? e.isPlainObject_ : !0,
      set: a
    }) === null ? 0 : 2 : (Z(r, t, {
      configurable: !0,
      set: a
    }), 2);
  }
  if (r !== e.target_ && typeof n.value == "function") {
    var s;
    if (ai(n.value)) {
      var l, c = (l = this.options_) != null && l.autoBind ? He.bound : He;
      return c.make_(e, t, n, r);
    }
    var u = (s = this.options_) != null && s.autoBind ? gt.bound : gt;
    return u.make_(e, t, n, r);
  }
  var d = ((i = this.options_) == null ? void 0 : i.deep) === !1 ? S.ref : S;
  if (typeof n.value == "function" && (o = this.options_) != null && o.autoBind) {
    var v;
    n.value = n.value.bind((v = e.proxy_) != null ? v : e.target_);
  }
  return d.make_(e, t, n, r);
}
function _a(e, t, n, r) {
  var i, o;
  if (n.get)
    return ln.extend_(e, t, n, r);
  if (n.set)
    return e.defineProperty_(t, {
      configurable: f.safeDescriptors ? e.isPlainObject_ : !0,
      set: Ce(t.toString(), n.set)
    }, r);
  if (typeof n.value == "function" && (i = this.options_) != null && i.autoBind) {
    var a;
    n.value = n.value.bind((a = e.proxy_) != null ? a : e.target_);
  }
  var s = ((o = this.options_) == null ? void 0 : o.deep) === !1 ? S.ref : S;
  return s.extend_(e, t, n, r);
}
function ya(e, t) {
  h("'" + this.annotationType_ + "' cannot be used as a decorator");
}
var wa = "observable", Ea = "observable.ref", Oa = "observable.shallow", Aa = "observable.struct", gi = {
  deep: !0,
  name: void 0,
  defaultDecorator: void 0,
  proxy: !0
};
Object.freeze(gi);
function Pt(e) {
  return e || gi;
}
var Dn = /* @__PURE__ */ sn(wa), Sa = /* @__PURE__ */ sn(Ea, {
  enhancer: an
}), xa = /* @__PURE__ */ sn(Oa, {
  enhancer: Yo
}), Na = /* @__PURE__ */ sn(Aa, {
  enhancer: Zo
}), mi = /* @__PURE__ */ Q(Dn);
function $t(e) {
  return e.deep === !0 ? Ne : e.deep === !1 ? an : Pa(e.defaultDecorator);
}
function Ca(e) {
  var t;
  return e ? (t = e.defaultDecorator) != null ? t : pi(e) : void 0;
}
function Pa(e) {
  var t, n;
  return e && (t = (n = e.options_) == null ? void 0 : n.enhancer) != null ? t : Ne;
}
function bi(e, t, n) {
  if (At(t))
    return Dn.decorate_20223_(e, t);
  if (xe(t)) {
    Ot(e, t, Dn);
    return;
  }
  return bt(e) ? e : P(e) ? S.object(e, t, n) : Array.isArray(e) ? S.array(e, t) : Ge(e) ? S.map(e, t) : re(e) ? S.set(e, t) : typeof e == "object" && e !== null ? e : S.box(e, t);
}
ii(bi, mi);
var $a = {
  box: function(t, n) {
    var r = Pt(n);
    return new Se(t, $t(r), r.name, !0, r.equals);
  },
  array: function(t, n) {
    var r = Pt(n);
    return (f.useProxies === !1 || r.proxy === !1 ? xs : gs)(t, $t(r), r.name);
  },
  map: function(t, n) {
    var r = Pt(n);
    return new Bi(t, $t(r), r.name);
  },
  set: function(t, n) {
    var r = Pt(n);
    return new Fi(t, $t(r), r.name);
  },
  object: function(t, n, r) {
    return Ie(function() {
      return Mi(f.useProxies === !1 || r?.proxy === !1 ? Ye({}, r) : hs({}, r), t, n);
    });
  },
  ref: /* @__PURE__ */ Q(Sa),
  shallow: /* @__PURE__ */ Q(xa),
  deep: mi,
  struct: /* @__PURE__ */ Q(Na)
}, S = /* @__PURE__ */ ii(bi, $a), _i = "computed", Da = "computed.struct", Tn = /* @__PURE__ */ Jn(_i), Ta = /* @__PURE__ */ Jn(Da, {
  equals: Be.structural
}), ln = function(t, n) {
  if (At(n))
    return Tn.decorate_20223_(t, n);
  if (xe(n))
    return Ot(t, n, Tn);
  if (P(t))
    return Q(Jn(_i, t));
  process.env.NODE_ENV !== "production" && (A(t) || h("First argument to `computed` should be an expression."), A(n) && h("A setter as second argument is no longer supported, use `{ set: fn }` option instead"));
  var r = P(n) ? n : {};
  return r.get = t, r.name || (r.name = t.name || ""), new U(r);
};
Object.assign(ln, Tn);
ln.struct = /* @__PURE__ */ Q(Ta);
var lr, cr, Ht = 0, ka = 1, Ia = (lr = (cr = /* @__PURE__ */ Ut(function() {
}, "name")) == null ? void 0 : cr.configurable) != null ? lr : !1, ur = {
  value: "action",
  configurable: !0,
  writable: !1,
  enumerable: !1
};
function Ce(e, t, n, r) {
  n === void 0 && (n = !1), process.env.NODE_ENV !== "production" && (A(t) || h("`action` can only be invoked on functions"), (typeof e != "string" || !e) && h("actions should have valid names, got: '" + e + "'"));
  function i() {
    return yi(e, n, t, r || this, arguments);
  }
  return i.isMobxAction = !0, i.toString = function() {
    return t.toString();
  }, Ia && (ur.value = e, Z(i, "name", ur)), i;
}
function yi(e, t, n, r, i) {
  var o = Va(e, t, r, i);
  try {
    return n.apply(r, i);
  } catch (a) {
    throw o.error_ = a, a;
  } finally {
    Ra(o);
  }
}
function Va(e, t, n, r) {
  var i = process.env.NODE_ENV !== "production" && C() && !!e, o = 0;
  if (process.env.NODE_ENV !== "production" && i) {
    o = Date.now();
    var a = r ? Array.from(r) : Bt;
    k({
      type: Yn,
      name: e,
      object: n,
      arguments: a
    });
  }
  var s = f.trackingDerivation, l = !t || !s;
  M();
  var c = f.allowStateChanges;
  l && (ke(), c = cn(!0));
  var u = Xn(!0), d = {
    runAsAction_: l,
    prevDerivation_: s,
    prevAllowStateChanges_: c,
    prevAllowStateReads_: u,
    notifySpy_: i,
    startTime_: o,
    actionId_: ka++,
    parentActionId_: Ht
  };
  return Ht = d.actionId_, d;
}
function Ra(e) {
  Ht !== e.actionId_ && h(30), Ht = e.parentActionId_, e.error_ !== void 0 && (f.suppressReactionErrors = !0), un(e.prevAllowStateChanges_), dt(e.prevAllowStateReads_), L(), e.runAsAction_ && ae(e.prevDerivation_), process.env.NODE_ENV !== "production" && e.notifySpy_ && I({
    time: Date.now() - e.startTime_
  }), f.suppressReactionErrors = !1;
}
function ja(e, t) {
  var n = cn(e);
  try {
    return t();
  } finally {
    un(n);
  }
}
function cn(e) {
  var t = f.allowStateChanges;
  return f.allowStateChanges = e, t;
}
function un(e) {
  f.allowStateChanges = e;
}
var Ma = "create", Se = /* @__PURE__ */ function(e) {
  function t(r, i, o, a, s) {
    var l;
    return o === void 0 && (o = process.env.NODE_ENV !== "production" ? "ObservableValue@" + B() : "ObservableValue"), a === void 0 && (a = !0), s === void 0 && (s = Be.default), l = e.call(this, o) || this, l.enhancer = void 0, l.name_ = void 0, l.equals = void 0, l.hasUnreportedChange_ = !1, l.interceptors_ = void 0, l.changeListeners_ = void 0, l.value_ = void 0, l.dehancer = void 0, l.enhancer = i, l.name_ = o, l.equals = s, l.value_ = i(r, void 0, o), process.env.NODE_ENV !== "production" && a && C() && Pe({
      type: Ma,
      object: l,
      observableKind: "value",
      debugObjectName: l.name_,
      newValue: "" + l.value_
    }), l;
  }
  ui(t, e);
  var n = t.prototype;
  return n.dehanceValue = function(i) {
    return this.dehancer !== void 0 ? this.dehancer(i) : i;
  }, n.set = function(i) {
    var o = this.value_;
    if (i = this.prepareNewValue_(i), i !== f.UNCHANGED) {
      var a = C();
      process.env.NODE_ENV !== "production" && a && k({
        type: F,
        object: this,
        observableKind: "value",
        debugObjectName: this.name_,
        newValue: i,
        oldValue: o
      }), this.setNewValue_(i), process.env.NODE_ENV !== "production" && a && I();
    }
  }, n.prepareNewValue_ = function(i) {
    if (Y(this), R(this)) {
      var o = j(this, {
        object: this,
        type: F,
        newValue: i
      });
      if (!o)
        return f.UNCHANGED;
      i = o.newValue;
    }
    return i = this.enhancer(i, this.value_, this.name_), this.equals(this.value_, i) ? f.UNCHANGED : i;
  }, n.setNewValue_ = function(i) {
    var o = this.value_;
    this.value_ = i, this.reportChanged(), K(this) && q(this, {
      type: F,
      object: this,
      newValue: i,
      oldValue: o
    });
  }, n.get = function() {
    return this.reportObserved(), this.dehanceValue(this.value_);
  }, n.intercept_ = function(i) {
    return xt(this, i);
  }, n.observe_ = function(i, o) {
    return o && i({
      observableKind: "value",
      debugObjectName: this.name_,
      object: this,
      type: F,
      newValue: this.value_,
      oldValue: void 0
    }), Nt(this, i);
  }, n.raw = function() {
    return this.value_;
  }, n.toJSON = function() {
    return this.get();
  }, n.toString = function() {
    return this.name_ + "[" + this.value_ + "]";
  }, n.valueOf = function() {
    return ci(this.get());
  }, n[Symbol.toPrimitive] = function() {
    return this.valueOf();
  }, t;
}(ge), U = /* @__PURE__ */ function() {
  function e(n) {
    this.dependenciesState_ = _.NOT_TRACKING_, this.observing_ = [], this.newObserving_ = null, this.observers_ = /* @__PURE__ */ new Set(), this.runId_ = 0, this.lastAccessedBy_ = 0, this.lowestObserverState_ = _.UP_TO_DATE_, this.unboundDepsCount_ = 0, this.value_ = new Kt(null), this.name_ = void 0, this.triggeredBy_ = void 0, this.flags_ = 0, this.derivation = void 0, this.setter_ = void 0, this.isTracing_ = z.NONE, this.scope_ = void 0, this.equals_ = void 0, this.requiresReaction_ = void 0, this.keepAlive_ = void 0, this.onBOL = void 0, this.onBUOL = void 0, n.get || h(31), this.derivation = n.get, this.name_ = n.name || (process.env.NODE_ENV !== "production" ? "ComputedValue@" + B() : "ComputedValue"), n.set && (this.setter_ = Ce(process.env.NODE_ENV !== "production" ? this.name_ + "-setter" : "ComputedValue-setter", n.set)), this.equals_ = n.equals || (n.compareStructural || n.struct ? Be.structural : Be.default), this.scope_ = n.context, this.requiresReaction_ = n.requiresReaction, this.keepAlive_ = !!n.keepAlive;
  }
  var t = e.prototype;
  return t.onBecomeStale_ = function() {
    Ha(this);
  }, t.onBO = function() {
    this.onBOL && this.onBOL.forEach(function(r) {
      return r();
    });
  }, t.onBUO = function() {
    this.onBUOL && this.onBUOL.forEach(function(r) {
      return r();
    });
  }, t.get = function() {
    if (this.isComputing && h(32, this.name_, this.derivation), f.inBatch === 0 && // !globalState.trackingDerivatpion &&
    this.observers_.size === 0 && !this.keepAlive_)
      kn(this) && (this.warnAboutUntrackedRead_(), M(), this.value_ = this.computeValue_(!1), L());
    else if (xi(this), kn(this)) {
      var r = f.trackingContext;
      this.keepAlive_ && !r && (f.trackingContext = this), this.trackAndCompute() && Fa(this), f.trackingContext = r;
    }
    var i = this.value_;
    if (jt(i))
      throw i.cause;
    return i;
  }, t.set = function(r) {
    if (this.setter_) {
      this.isRunningSetter && h(33, this.name_), this.isRunningSetter = !0;
      try {
        this.setter_.call(this.scope_, r);
      } finally {
        this.isRunningSetter = !1;
      }
    } else
      h(34, this.name_);
  }, t.trackAndCompute = function() {
    var r = this.value_, i = (
      /* see #1208 */
      this.dependenciesState_ === _.NOT_TRACKING_
    ), o = this.computeValue_(!0), a = i || jt(r) || jt(o) || !this.equals_(r, o);
    return a && (this.value_ = o, process.env.NODE_ENV !== "production" && C() && Pe({
      observableKind: "computed",
      debugObjectName: this.name_,
      object: this.scope_,
      type: "update",
      oldValue: r,
      newValue: o
    })), a;
  }, t.computeValue_ = function(r) {
    this.isComputing = !0;
    var i = cn(!1), o;
    if (r)
      o = wi(this, this.derivation, this.scope_);
    else if (f.disableErrorBoundaries === !0)
      o = this.derivation.call(this.scope_);
    else
      try {
        o = this.derivation.call(this.scope_);
      } catch (a) {
        o = new Kt(a);
      }
    return un(i), this.isComputing = !1, o;
  }, t.suspend_ = function() {
    this.keepAlive_ || (In(this), this.value_ = void 0, process.env.NODE_ENV !== "production" && this.isTracing_ !== z.NONE && console.log("[mobx.trace] Computed value '" + this.name_ + "' was suspended and it will recompute on the next access."));
  }, t.observe_ = function(r, i) {
    var o = this, a = !0, s = void 0;
    return Ii(function() {
      var l = o.get();
      if (!a || i) {
        var c = ke();
        r({
          observableKind: "computed",
          debugObjectName: o.name_,
          type: F,
          object: o,
          newValue: l,
          oldValue: s
        }), ae(c);
      }
      a = !1, s = l;
    });
  }, t.warnAboutUntrackedRead_ = function() {
    process.env.NODE_ENV !== "production" && (this.isTracing_ !== z.NONE && console.log("[mobx.trace] Computed value '" + this.name_ + "' is being read outside a reactive context. Doing a full recompute."), (typeof this.requiresReaction_ == "boolean" ? this.requiresReaction_ : f.computedRequiresReaction) && console.warn("[mobx] Computed value '" + this.name_ + "' is being read outside a reactive context. Doing a full recompute."));
  }, t.toString = function() {
    return this.name_ + "[" + this.derivation.toString() + "]";
  }, t.valueOf = function() {
    return ci(this.get());
  }, t[Symbol.toPrimitive] = function() {
    return this.valueOf();
  }, Je(e, [{
    key: "isComputing",
    get: function() {
      return D(this.flags_, e.isComputingMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isComputingMask_, r);
    }
  }, {
    key: "isRunningSetter",
    get: function() {
      return D(this.flags_, e.isRunningSetterMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isRunningSetterMask_, r);
    }
  }, {
    key: "isBeingObserved",
    get: function() {
      return D(this.flags_, e.isBeingObservedMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isBeingObservedMask_, r);
    }
  }, {
    key: "isPendingUnobservation",
    get: function() {
      return D(this.flags_, e.isPendingUnobservationMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isPendingUnobservationMask_, r);
    }
  }, {
    key: "diffValue",
    get: function() {
      return D(this.flags_, e.diffValueMask_) ? 1 : 0;
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.diffValueMask_, r === 1);
    }
  }]);
}();
U.isComputingMask_ = 1;
U.isRunningSetterMask_ = 2;
U.isBeingObservedMask_ = 4;
U.isPendingUnobservationMask_ = 8;
U.diffValueMask_ = 16;
var dn = /* @__PURE__ */ Te("ComputedValue", U), _;
(function(e) {
  e[e.NOT_TRACKING_ = -1] = "NOT_TRACKING_", e[e.UP_TO_DATE_ = 0] = "UP_TO_DATE_", e[e.POSSIBLY_STALE_ = 1] = "POSSIBLY_STALE_", e[e.STALE_ = 2] = "STALE_";
})(_ || (_ = {}));
var z;
(function(e) {
  e[e.NONE = 0] = "NONE", e[e.LOG = 1] = "LOG", e[e.BREAK = 2] = "BREAK";
})(z || (z = {}));
var Kt = function(t) {
  this.cause = void 0, this.cause = t;
};
function jt(e) {
  return e instanceof Kt;
}
function kn(e) {
  switch (e.dependenciesState_) {
    case _.UP_TO_DATE_:
      return !1;
    case _.NOT_TRACKING_:
    case _.STALE_:
      return !0;
    case _.POSSIBLY_STALE_: {
      for (var t = Xn(!0), n = ke(), r = e.observing_, i = r.length, o = 0; o < i; o++) {
        var a = r[o];
        if (dn(a)) {
          if (f.disableErrorBoundaries)
            a.get();
          else
            try {
              a.get();
            } catch {
              return ae(n), dt(t), !0;
            }
          if (e.dependenciesState_ === _.STALE_)
            return ae(n), dt(t), !0;
        }
      }
      return Oi(e), ae(n), dt(t), !1;
    }
  }
}
function Y(e) {
  if (process.env.NODE_ENV !== "production") {
    var t = e.observers_.size > 0;
    !f.allowStateChanges && (t || f.enforceActions === "always") && console.warn("[MobX] " + (f.enforceActions ? "Since strict-mode is enabled, changing (observed) observable values without using an action is not allowed. Tried to modify: " : "Side effects like changing state are not allowed at this point. Are you trying to modify state from, for example, a computed value or the render function of a React component? You can wrap side effects in 'runInAction' (or decorate functions with 'action') if needed. Tried to modify: ") + e.name_);
  }
}
function La(e) {
  process.env.NODE_ENV !== "production" && !f.allowStateReads && f.observableRequiresReaction && console.warn("[mobx] Observable '" + e.name_ + "' being read outside a reactive context.");
}
function wi(e, t, n) {
  var r = Xn(!0);
  Oi(e), e.newObserving_ = new Array(
    // Reserve constant space for initial dependencies, dynamic space otherwise.
    // See https://github.com/mobxjs/mobx/pull/3833
    e.runId_ === 0 ? 100 : e.observing_.length
  ), e.unboundDepsCount_ = 0, e.runId_ = ++f.runId;
  var i = f.trackingDerivation;
  f.trackingDerivation = e, f.inBatch++;
  var o;
  if (f.disableErrorBoundaries === !0)
    o = t.call(n);
  else
    try {
      o = t.call(n);
    } catch (a) {
      o = new Kt(a);
    }
  return f.inBatch--, f.trackingDerivation = i, Ua(e), za(e), dt(r), o;
}
function za(e) {
  process.env.NODE_ENV !== "production" && e.observing_.length === 0 && (typeof e.requiresObservable_ == "boolean" ? e.requiresObservable_ : f.reactionRequiresObservable) && console.warn("[mobx] Derivation '" + e.name_ + "' is created/updated without reading any observable value.");
}
function Ua(e) {
  for (var t = e.observing_, n = e.observing_ = e.newObserving_, r = _.UP_TO_DATE_, i = 0, o = e.unboundDepsCount_, a = 0; a < o; a++) {
    var s = n[a];
    s.diffValue === 0 && (s.diffValue = 1, i !== a && (n[i] = s), i++), s.dependenciesState_ > r && (r = s.dependenciesState_);
  }
  for (n.length = i, e.newObserving_ = null, o = t.length; o--; ) {
    var l = t[o];
    l.diffValue === 0 && Ai(l, e), l.diffValue = 0;
  }
  for (; i--; ) {
    var c = n[i];
    c.diffValue === 1 && (c.diffValue = 0, Ba(c, e));
  }
  r !== _.UP_TO_DATE_ && (e.dependenciesState_ = r, e.onBecomeStale_());
}
function In(e) {
  var t = e.observing_;
  e.observing_ = [];
  for (var n = t.length; n--; )
    Ai(t[n], e);
  e.dependenciesState_ = _.NOT_TRACKING_;
}
function Ei(e) {
  var t = ke();
  try {
    return e();
  } finally {
    ae(t);
  }
}
function ke() {
  var e = f.trackingDerivation;
  return f.trackingDerivation = null, e;
}
function ae(e) {
  f.trackingDerivation = e;
}
function Xn(e) {
  var t = f.allowStateReads;
  return f.allowStateReads = e, t;
}
function dt(e) {
  f.allowStateReads = e;
}
function Oi(e) {
  if (e.dependenciesState_ !== _.UP_TO_DATE_) {
    e.dependenciesState_ = _.UP_TO_DATE_;
    for (var t = e.observing_, n = t.length; n--; )
      t[n].lowestObserverState_ = _.UP_TO_DATE_;
  }
}
var mn = function() {
  this.version = 6, this.UNCHANGED = {}, this.trackingDerivation = null, this.trackingContext = null, this.runId = 0, this.mobxGuid = 0, this.inBatch = 0, this.pendingUnobservations = [], this.pendingReactions = [], this.isRunningReactions = !1, this.allowStateChanges = !1, this.allowStateReads = !0, this.enforceActions = !0, this.spyListeners = [], this.globalReactionErrorHandlers = [], this.computedRequiresReaction = !1, this.reactionRequiresObservable = !1, this.observableRequiresReaction = !1, this.disableErrorBoundaries = !1, this.suppressReactionErrors = !1, this.useProxies = !0, this.verifyProxies = !1, this.safeDescriptors = !0;
}, bn = !0, f = /* @__PURE__ */ function() {
  var e = /* @__PURE__ */ Kn();
  return e.__mobxInstanceCount > 0 && !e.__mobxGlobals && (bn = !1), e.__mobxGlobals && e.__mobxGlobals.version !== new mn().version && (bn = !1), bn ? e.__mobxGlobals ? (e.__mobxInstanceCount += 1, e.__mobxGlobals.UNCHANGED || (e.__mobxGlobals.UNCHANGED = {}), e.__mobxGlobals) : (e.__mobxInstanceCount = 1, e.__mobxGlobals = /* @__PURE__ */ new mn()) : (setTimeout(function() {
    h(35);
  }, 1), new mn());
}();
function Ba(e, t) {
  e.observers_.add(t), e.lowestObserverState_ > t.dependenciesState_ && (e.lowestObserverState_ = t.dependenciesState_);
}
function Ai(e, t) {
  e.observers_.delete(t), e.observers_.size === 0 && Si(e);
}
function Si(e) {
  e.isPendingUnobservation === !1 && (e.isPendingUnobservation = !0, f.pendingUnobservations.push(e));
}
function M() {
  f.inBatch++;
}
function L() {
  if (--f.inBatch === 0) {
    $i();
    for (var e = f.pendingUnobservations, t = 0; t < e.length; t++) {
      var n = e[t];
      n.isPendingUnobservation = !1, n.observers_.size === 0 && (n.isBeingObserved && (n.isBeingObserved = !1, n.onBUO()), n instanceof U && n.suspend_());
    }
    f.pendingUnobservations = [];
  }
}
function xi(e) {
  La(e);
  var t = f.trackingDerivation;
  return t !== null ? (t.runId_ !== e.lastAccessedBy_ && (e.lastAccessedBy_ = t.runId_, t.newObserving_[t.unboundDepsCount_++] = e, !e.isBeingObserved && f.trackingContext && (e.isBeingObserved = !0, e.onBO())), e.isBeingObserved) : (e.observers_.size === 0 && f.inBatch > 0 && Si(e), !1);
}
function Ni(e) {
  e.lowestObserverState_ !== _.STALE_ && (e.lowestObserverState_ = _.STALE_, e.observers_.forEach(function(t) {
    t.dependenciesState_ === _.UP_TO_DATE_ && (process.env.NODE_ENV !== "production" && t.isTracing_ !== z.NONE && Ci(t, e), t.onBecomeStale_()), t.dependenciesState_ = _.STALE_;
  }));
}
function Fa(e) {
  e.lowestObserverState_ !== _.STALE_ && (e.lowestObserverState_ = _.STALE_, e.observers_.forEach(function(t) {
    t.dependenciesState_ === _.POSSIBLY_STALE_ ? (t.dependenciesState_ = _.STALE_, process.env.NODE_ENV !== "production" && t.isTracing_ !== z.NONE && Ci(t, e)) : t.dependenciesState_ === _.UP_TO_DATE_ && (e.lowestObserverState_ = _.UP_TO_DATE_);
  }));
}
function Ha(e) {
  e.lowestObserverState_ === _.UP_TO_DATE_ && (e.lowestObserverState_ = _.POSSIBLY_STALE_, e.observers_.forEach(function(t) {
    t.dependenciesState_ === _.UP_TO_DATE_ && (t.dependenciesState_ = _.POSSIBLY_STALE_, t.onBecomeStale_());
  }));
}
function Ci(e, t) {
  if (console.log("[mobx.trace] '" + e.name_ + "' is invalidated due to a change in: '" + t.name_ + "'"), e.isTracing_ === z.BREAK) {
    var n = [];
    Pi(os(e), n, 1), new Function(`debugger;
/*
Tracing '` + e.name_ + `'

You are entering this break point because derivation '` + e.name_ + "' is being traced and '" + t.name_ + `' is now forcing it to update.
Just follow the stacktrace you should now see in the devtools to see precisely what piece of your code is causing this update
The stackframe you are looking for is at least ~6-8 stack-frames up.

` + (e instanceof U ? e.derivation.toString().replace(/[*]\//g, "/") : "") + `

The dependencies for this derivation are:

` + n.join(`
`) + `
*/
    `)();
  }
}
function Pi(e, t, n) {
  if (t.length >= 1e3) {
    t.push("(and many more)");
    return;
  }
  t.push("" + "	".repeat(n - 1) + e.name), e.dependencies && e.dependencies.forEach(function(r) {
    return Pi(r, t, n + 1);
  });
}
var ee = /* @__PURE__ */ function() {
  function e(n, r, i, o) {
    n === void 0 && (n = process.env.NODE_ENV !== "production" ? "Reaction@" + B() : "Reaction"), this.name_ = void 0, this.onInvalidate_ = void 0, this.errorHandler_ = void 0, this.requiresObservable_ = void 0, this.observing_ = [], this.newObserving_ = [], this.dependenciesState_ = _.NOT_TRACKING_, this.runId_ = 0, this.unboundDepsCount_ = 0, this.flags_ = 0, this.isTracing_ = z.NONE, this.name_ = n, this.onInvalidate_ = r, this.errorHandler_ = i, this.requiresObservable_ = o;
  }
  var t = e.prototype;
  return t.onBecomeStale_ = function() {
    this.schedule_();
  }, t.schedule_ = function() {
    this.isScheduled || (this.isScheduled = !0, f.pendingReactions.push(this), $i());
  }, t.runReaction_ = function() {
    if (!this.isDisposed) {
      M(), this.isScheduled = !1;
      var r = f.trackingContext;
      if (f.trackingContext = this, kn(this)) {
        this.isTrackPending = !0;
        try {
          this.onInvalidate_(), process.env.NODE_ENV !== "production" && this.isTrackPending && C() && Pe({
            name: this.name_,
            type: "scheduled-reaction"
          });
        } catch (i) {
          this.reportExceptionInDerivation_(i);
        }
      }
      f.trackingContext = r, L();
    }
  }, t.track = function(r) {
    if (!this.isDisposed) {
      M();
      var i = C(), o;
      process.env.NODE_ENV !== "production" && i && (o = Date.now(), k({
        name: this.name_,
        type: "reaction"
      })), this.isRunning = !0;
      var a = f.trackingContext;
      f.trackingContext = this;
      var s = wi(this, r, void 0);
      f.trackingContext = a, this.isRunning = !1, this.isTrackPending = !1, this.isDisposed && In(this), jt(s) && this.reportExceptionInDerivation_(s.cause), process.env.NODE_ENV !== "production" && i && I({
        time: Date.now() - o
      }), L();
    }
  }, t.reportExceptionInDerivation_ = function(r) {
    var i = this;
    if (this.errorHandler_) {
      this.errorHandler_(r, this);
      return;
    }
    if (f.disableErrorBoundaries)
      throw r;
    var o = process.env.NODE_ENV !== "production" ? "[mobx] Encountered an uncaught exception that was thrown by a reaction or observer component, in: '" + this + "'" : "[mobx] uncaught error in '" + this + "'";
    f.suppressReactionErrors ? process.env.NODE_ENV !== "production" && console.warn("[mobx] (error in reaction '" + this.name_ + "' suppressed, fix error of causing action below)") : console.error(o, r), process.env.NODE_ENV !== "production" && C() && Pe({
      type: "error",
      name: this.name_,
      message: o,
      error: "" + r
    }), f.globalReactionErrorHandlers.forEach(function(a) {
      return a(r, i);
    });
  }, t.dispose = function() {
    this.isDisposed || (this.isDisposed = !0, this.isRunning || (M(), In(this), L()));
  }, t.getDisposer_ = function(r) {
    var i = this, o = function a() {
      i.dispose(), r == null || r.removeEventListener == null || r.removeEventListener("abort", a);
    };
    return r == null || r.addEventListener == null || r.addEventListener("abort", o), o[m] = this, o;
  }, t.toString = function() {
    return "Reaction[" + this.name_ + "]";
  }, t.trace = function(r) {
    r === void 0 && (r = !1), us(this, r);
  }, Je(e, [{
    key: "isDisposed",
    get: function() {
      return D(this.flags_, e.isDisposedMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isDisposedMask_, r);
    }
  }, {
    key: "isScheduled",
    get: function() {
      return D(this.flags_, e.isScheduledMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isScheduledMask_, r);
    }
  }, {
    key: "isTrackPending",
    get: function() {
      return D(this.flags_, e.isTrackPendingMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isTrackPendingMask_, r);
    }
  }, {
    key: "isRunning",
    get: function() {
      return D(this.flags_, e.isRunningMask_);
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.isRunningMask_, r);
    }
  }, {
    key: "diffValue",
    get: function() {
      return D(this.flags_, e.diffValueMask_) ? 1 : 0;
    },
    set: function(r) {
      this.flags_ = T(this.flags_, e.diffValueMask_, r === 1);
    }
  }]);
}();
ee.isDisposedMask_ = 1;
ee.isScheduledMask_ = 2;
ee.isTrackPendingMask_ = 4;
ee.isRunningMask_ = 8;
ee.diffValueMask_ = 16;
function Ka(e) {
  return f.globalReactionErrorHandlers.push(e), function() {
    var t = f.globalReactionErrorHandlers.indexOf(e);
    t >= 0 && f.globalReactionErrorHandlers.splice(t, 1);
  };
}
var dr = 100, qa = function(t) {
  return t();
};
function $i() {
  f.inBatch > 0 || f.isRunningReactions || qa(Wa);
}
function Wa() {
  f.isRunningReactions = !0;
  for (var e = f.pendingReactions, t = 0; e.length > 0; ) {
    ++t === dr && (console.error(process.env.NODE_ENV !== "production" ? "Reaction doesn't converge to a stable state after " + dr + " iterations." + (" Probably there is a cycle in the reactive function: " + e[0]) : "[mobx] cycle in reaction: " + e[0]), e.splice(0));
    for (var n = e.splice(0), r = 0, i = n.length; r < i; r++)
      n[r].runReaction_();
  }
  f.isRunningReactions = !1;
}
var qt = /* @__PURE__ */ Te("Reaction", ee);
function C() {
  return process.env.NODE_ENV !== "production" && !!f.spyListeners.length;
}
function Pe(e) {
  if (process.env.NODE_ENV !== "production" && f.spyListeners.length)
    for (var t = f.spyListeners, n = 0, r = t.length; n < r; n++)
      t[n](e);
}
function k(e) {
  if (process.env.NODE_ENV !== "production") {
    var t = he({}, e, {
      spyReportStart: !0
    });
    Pe(t);
  }
}
var Ga = {
  type: "report-end",
  spyReportEnd: !0
};
function I(e) {
  process.env.NODE_ENV !== "production" && Pe(e ? he({}, e, {
    type: "report-end",
    spyReportEnd: !0
  }) : Ga);
}
function Ja(e) {
  return process.env.NODE_ENV === "production" ? (console.warn("[mobx.spy] Is a no-op in production builds"), function() {
  }) : (f.spyListeners.push(e), Wn(function() {
    f.spyListeners = f.spyListeners.filter(function(t) {
      return t !== e;
    });
  }));
}
var Yn = "action", Xa = "action.bound", Di = "autoAction", Ya = "autoAction.bound", Ti = "<unnamed action>", Vn = /* @__PURE__ */ St(Yn), Za = /* @__PURE__ */ St(Xa, {
  bound: !0
}), Rn = /* @__PURE__ */ St(Di, {
  autoAction: !0
}), Qa = /* @__PURE__ */ St(Ya, {
  autoAction: !0,
  bound: !0
});
function ki(e) {
  var t = function(r, i) {
    if (A(r))
      return Ce(r.name || Ti, r, e);
    if (A(i))
      return Ce(r, i, e);
    if (At(i))
      return (e ? Rn : Vn).decorate_20223_(r, i);
    if (xe(i))
      return Ot(r, i, e ? Rn : Vn);
    if (xe(r))
      return Q(St(e ? Di : Yn, {
        name: r,
        autoAction: e
      }));
    process.env.NODE_ENV !== "production" && h("Invalid arguments for `action`");
  };
  return t;
}
var Oe = /* @__PURE__ */ ki(!1);
Object.assign(Oe, Vn);
var gt = /* @__PURE__ */ ki(!0);
Object.assign(gt, Rn);
Oe.bound = /* @__PURE__ */ Q(Za);
gt.bound = /* @__PURE__ */ Q(Qa);
function ru(e) {
  return yi(e.name || Ti, !1, e, this, void 0);
}
function Fe(e) {
  return A(e) && e.isMobxAction === !0;
}
function Ii(e, t) {
  var n, r, i, o;
  t === void 0 && (t = qn), process.env.NODE_ENV !== "production" && (A(e) || h("Autorun expects a function as first argument"), Fe(e) && h("Autorun does not accept actions since actions are untrackable"));
  var a = (n = (r = t) == null ? void 0 : r.name) != null ? n : process.env.NODE_ENV !== "production" ? e.name || "Autorun@" + B() : "Autorun", s = !t.scheduler && !t.delay, l;
  if (s)
    l = new ee(a, function() {
      this.track(d);
    }, t.onError, t.requiresObservable);
  else {
    var c = Vi(t), u = !1;
    l = new ee(a, function() {
      u || (u = !0, c(function() {
        u = !1, l.isDisposed || l.track(d);
      }));
    }, t.onError, t.requiresObservable);
  }
  function d() {
    e(l);
  }
  return (i = t) != null && (i = i.signal) != null && i.aborted || l.schedule_(), l.getDisposer_((o = t) == null ? void 0 : o.signal);
}
var es = function(t) {
  return t();
};
function Vi(e) {
  return e.scheduler ? e.scheduler : e.delay ? function(t) {
    return setTimeout(t, e.delay);
  } : es;
}
function Zn(e, t, n) {
  var r, i, o;
  n === void 0 && (n = qn), process.env.NODE_ENV !== "production" && ((!A(e) || !A(t)) && h("First and second argument to reaction should be functions"), P(n) || h("Third argument of reactions should be an object"));
  var a = (r = n.name) != null ? r : process.env.NODE_ENV !== "production" ? "Reaction@" + B() : "Reaction", s = Oe(a, n.onError ? ts(n.onError, t) : t), l = !n.scheduler && !n.delay, c = Vi(n), u = !0, d = !1, v, p = n.compareStructural ? Be.structural : n.equals || Be.default, b = new ee(a, function() {
    u || l ? E() : d || (d = !0, c(E));
  }, n.onError, n.requiresObservable);
  function E() {
    if (d = !1, !b.isDisposed) {
      var x = !1, G = v;
      b.track(function() {
        var Re = ja(!1, function() {
          return e(b);
        });
        x = u || !p(v, Re), v = Re;
      }), (u && n.fireImmediately || !u && x) && s(v, G, b), u = !1;
    }
  }
  return (i = n) != null && (i = i.signal) != null && i.aborted || b.schedule_(), b.getDisposer_((o = n) == null ? void 0 : o.signal);
}
function ts(e, t) {
  return function() {
    try {
      return t.apply(this, arguments);
    } catch (n) {
      e.call(this, n);
    }
  };
}
var ns = "onBO", rs = "onBUO";
function is(e, t, n) {
  return ji(ns, e, t, n);
}
function Ri(e, t, n) {
  return ji(rs, e, t, n);
}
function ji(e, t, n, r) {
  var i = Ke(t), o = A(r) ? r : n, a = e + "L";
  return i[a] ? i[a].add(o) : i[a] = /* @__PURE__ */ new Set([o]), function() {
    var s = i[a];
    s && (s.delete(o), s.size === 0 && delete i[a]);
  };
}
function Mi(e, t, n, r) {
  process.env.NODE_ENV !== "production" && (arguments.length > 4 && h("'extendObservable' expected 2-4 arguments"), typeof e != "object" && h("'extendObservable' expects an object as first argument"), me(e) && h("'extendObservable' should not be used on maps, use map.merge instead"), P(t) || h("'extendObservable' only accepts plain objects as second argument"), (bt(t) || bt(n)) && h("Extending an object with another observable (object) is not supported"));
  var i = Fo(t);
  return Ie(function() {
    var o = Ye(e, r)[m];
    pt(i).forEach(function(a) {
      o.extend_(
        a,
        i[a],
        // must pass "undefined" for { key: undefined }
        n && a in n ? n[a] : !0
      );
    });
  }), e;
}
function os(e, t) {
  return Li(Ke(e, t));
}
function Li(e) {
  var t = {
    name: e.name_
  };
  return e.observing_ && e.observing_.length > 0 && (t.dependencies = as(e.observing_).map(Li)), t;
}
function as(e) {
  return Array.from(new Set(e));
}
var ss = 0;
function zi() {
  this.message = "FLOW_CANCELLED";
}
zi.prototype = /* @__PURE__ */ Object.create(Error.prototype);
var _n = /* @__PURE__ */ hi("flow"), ls = /* @__PURE__ */ hi("flow.bound", {
  bound: !0
}), He = /* @__PURE__ */ Object.assign(function(t, n) {
  if (At(n))
    return _n.decorate_20223_(t, n);
  if (xe(n))
    return Ot(t, n, _n);
  process.env.NODE_ENV !== "production" && arguments.length !== 1 && h("Flow expects single argument with generator function");
  var r = t, i = r.name || "<unnamed flow>", o = function() {
    var s = this, l = arguments, c = ++ss, u = Oe(i + " - runid: " + c + " - init", r).apply(s, l), d, v = void 0, p = new Promise(function(b, E) {
      var x = 0;
      d = E;
      function G($) {
        v = void 0;
        var ce;
        try {
          ce = Oe(i + " - runid: " + c + " - yield " + x++, u.next).call(u, $);
        } catch (be) {
          return E(be);
        }
        et(ce);
      }
      function Re($) {
        v = void 0;
        var ce;
        try {
          ce = Oe(i + " - runid: " + c + " - yield " + x++, u.throw).call(u, $);
        } catch (be) {
          return E(be);
        }
        et(ce);
      }
      function et($) {
        if (A($?.then)) {
          $.then(et, E);
          return;
        }
        return $.done ? b($.value) : (v = Promise.resolve($.value), v.then(G, Re));
      }
      G(void 0);
    });
    return p.cancel = Oe(i + " - runid: " + c + " - cancel", function() {
      try {
        v && fr(v);
        var b = u.return(void 0), E = Promise.resolve(b.value);
        E.then(Le, Le), fr(E), d(new zi());
      } catch (x) {
        d(x);
      }
    }), p;
  };
  return o.isMobXFlow = !0, o;
}, _n);
He.bound = /* @__PURE__ */ Q(ls);
function fr(e) {
  A(e.cancel) && e.cancel();
}
function mt(e) {
  return e?.isMobXFlow === !0;
}
function cs(e, t) {
  return e ? Ze(e) || !!e[m] || Gn(e) || qt(e) || dn(e) : !1;
}
function bt(e) {
  return process.env.NODE_ENV !== "production" && arguments.length !== 1 && h("isObservable expects only 1 argument. Use isObservableProp to inspect the observability of a property"), cs(e);
}
function us() {
  if (process.env.NODE_ENV !== "production") {
    for (var e = !1, t = arguments.length, n = new Array(t), r = 0; r < t; r++)
      n[r] = arguments[r];
    typeof n[n.length - 1] == "boolean" && (e = n.pop());
    var i = ds(n);
    if (!i)
      return h("'trace(break?)' can only be used inside a tracked computed value or a Reaction. Consider passing in the computed value or reaction explicitly");
    i.isTracing_ === z.NONE && console.log("[mobx.trace] '" + i.name_ + "' tracing enabled"), i.isTracing_ = e ? z.BREAK : z.LOG;
  }
}
function ds(e) {
  switch (e.length) {
    case 0:
      return f.trackingDerivation;
    case 1:
      return Ke(e[0]);
    case 2:
      return Ke(e[0], e[1]);
  }
}
function oe(e, t) {
  t === void 0 && (t = void 0), M();
  try {
    return e.apply(t);
  } finally {
    L();
  }
}
function _e(e) {
  return e[m];
}
var fs = {
  has: function(t, n) {
    return process.env.NODE_ENV !== "production" && f.trackingDerivation && tt("detect new properties using the 'in' operator. Use 'has' from 'mobx' instead."), _e(t).has_(n);
  },
  get: function(t, n) {
    return _e(t).get_(n);
  },
  set: function(t, n, r) {
    var i;
    return xe(n) ? (process.env.NODE_ENV !== "production" && !_e(t).values_.has(n) && tt("add a new observable property through direct assignment. Use 'set' from 'mobx' instead."), (i = _e(t).set_(n, r, !0)) != null ? i : !0) : !1;
  },
  deleteProperty: function(t, n) {
    var r;
    return process.env.NODE_ENV !== "production" && tt("delete properties from an observable object. Use 'remove' from 'mobx' instead."), xe(n) ? (r = _e(t).delete_(n, !0)) != null ? r : !0 : !1;
  },
  defineProperty: function(t, n, r) {
    var i;
    return process.env.NODE_ENV !== "production" && tt("define property on an observable object. Use 'defineProperty' from 'mobx' instead."), (i = _e(t).defineProperty_(n, r)) != null ? i : !0;
  },
  ownKeys: function(t) {
    return process.env.NODE_ENV !== "production" && f.trackingDerivation && tt("iterate keys to detect added / removed properties. Use 'keys' from 'mobx' instead."), _e(t).ownKeys_();
  },
  preventExtensions: function(t) {
    h(13);
  }
};
function hs(e, t) {
  var n, r;
  return oi(), e = Ye(e, t), (r = (n = e[m]).proxy_) != null ? r : n.proxy_ = new Proxy(e, fs);
}
function R(e) {
  return e.interceptors_ !== void 0 && e.interceptors_.length > 0;
}
function xt(e, t) {
  var n = e.interceptors_ || (e.interceptors_ = []);
  return n.push(t), Wn(function() {
    var r = n.indexOf(t);
    r !== -1 && n.splice(r, 1);
  });
}
function j(e, t) {
  var n = ke();
  try {
    for (var r = [].concat(e.interceptors_ || []), i = 0, o = r.length; i < o && (t = r[i](t), t && !t.type && h(14), !!t); i++)
      ;
    return t;
  } finally {
    ae(n);
  }
}
function K(e) {
  return e.changeListeners_ !== void 0 && e.changeListeners_.length > 0;
}
function Nt(e, t) {
  var n = e.changeListeners_ || (e.changeListeners_ = []);
  return n.push(t), Wn(function() {
    var r = n.indexOf(t);
    r !== -1 && n.splice(r, 1);
  });
}
function q(e, t) {
  var n = ke(), r = e.changeListeners_;
  if (r) {
    r = r.slice();
    for (var i = 0, o = r.length; i < o; i++)
      r[i](t);
    ae(n);
  }
}
var yn = /* @__PURE__ */ Symbol("mobx-keys");
function Xe(e, t, n) {
  return process.env.NODE_ENV !== "production" && (!P(e) && !P(Object.getPrototypeOf(e)) && h("'makeAutoObservable' can only be used for classes that don't have a superclass"), Ze(e) && h("makeAutoObservable can only be used on objects not already made observable")), P(e) ? Mi(e, e, t, n) : (Ie(function() {
    var r = Ye(e, n)[m];
    if (!e[yn]) {
      var i = Object.getPrototypeOf(e), o = new Set([].concat(pt(e), pt(i)));
      o.delete("constructor"), o.delete(m), rn(i, yn, o);
    }
    e[yn].forEach(function(a) {
      return r.make_(
        a,
        // must pass "undefined" for { key: undefined }
        t && a in t ? t[a] : !0
      );
    });
  }), e);
}
var hr = "splice", F = "update", vs = 1e4, ps = {
  get: function(t, n) {
    var r = t[m];
    return n === m ? r : n === "length" ? r.getArrayLength_() : typeof n == "string" && !isNaN(n) ? r.get_(parseInt(n)) : H(Wt, n) ? Wt[n] : t[n];
  },
  set: function(t, n, r) {
    var i = t[m];
    return n === "length" && i.setArrayLength_(r), typeof n == "symbol" || isNaN(n) ? t[n] = r : i.set_(parseInt(n), r), !0;
  },
  preventExtensions: function() {
    h(15);
  }
}, Qn = /* @__PURE__ */ function() {
  function e(n, r, i, o) {
    n === void 0 && (n = process.env.NODE_ENV !== "production" ? "ObservableArray@" + B() : "ObservableArray"), this.owned_ = void 0, this.legacyMode_ = void 0, this.atom_ = void 0, this.values_ = [], this.interceptors_ = void 0, this.changeListeners_ = void 0, this.enhancer_ = void 0, this.dehancer = void 0, this.proxy_ = void 0, this.lastKnownLength_ = 0, this.owned_ = i, this.legacyMode_ = o, this.atom_ = new ge(n), this.enhancer_ = function(a, s) {
      return r(a, s, process.env.NODE_ENV !== "production" ? n + "[..]" : "ObservableArray[..]");
    };
  }
  var t = e.prototype;
  return t.dehanceValue_ = function(r) {
    return this.dehancer !== void 0 ? this.dehancer(r) : r;
  }, t.dehanceValues_ = function(r) {
    return this.dehancer !== void 0 && r.length > 0 ? r.map(this.dehancer) : r;
  }, t.intercept_ = function(r) {
    return xt(this, r);
  }, t.observe_ = function(r, i) {
    return i === void 0 && (i = !1), i && r({
      observableKind: "array",
      object: this.proxy_,
      debugObjectName: this.atom_.name_,
      type: "splice",
      index: 0,
      added: this.values_.slice(),
      addedCount: this.values_.length,
      removed: [],
      removedCount: 0
    }), Nt(this, r);
  }, t.getArrayLength_ = function() {
    return this.atom_.reportObserved(), this.values_.length;
  }, t.setArrayLength_ = function(r) {
    (typeof r != "number" || isNaN(r) || r < 0) && h("Out of range: " + r);
    var i = this.values_.length;
    if (r !== i)
      if (r > i) {
        for (var o = new Array(r - i), a = 0; a < r - i; a++)
          o[a] = void 0;
        this.spliceWithArray_(i, 0, o);
      } else
        this.spliceWithArray_(r, i - r);
  }, t.updateArrayLength_ = function(r, i) {
    r !== this.lastKnownLength_ && h(16), this.lastKnownLength_ += i, this.legacyMode_ && i > 0 && qi(r + i + 1);
  }, t.spliceWithArray_ = function(r, i, o) {
    var a = this;
    Y(this.atom_);
    var s = this.values_.length;
    if (r === void 0 ? r = 0 : r > s ? r = s : r < 0 && (r = Math.max(0, s + r)), arguments.length === 1 ? i = s - r : i == null ? i = 0 : i = Math.max(0, Math.min(i, s - r)), o === void 0 && (o = Bt), R(this)) {
      var l = j(this, {
        object: this.proxy_,
        type: hr,
        index: r,
        removedCount: i,
        added: o
      });
      if (!l)
        return Bt;
      i = l.removedCount, o = l.added;
    }
    if (o = o.length === 0 ? o : o.map(function(d) {
      return a.enhancer_(d, void 0);
    }), this.legacyMode_ || process.env.NODE_ENV !== "production") {
      var c = o.length - i;
      this.updateArrayLength_(s, c);
    }
    var u = this.spliceItemsIntoValues_(r, i, o);
    return (i !== 0 || o.length !== 0) && this.notifyArraySplice_(r, o, u), this.dehanceValues_(u);
  }, t.spliceItemsIntoValues_ = function(r, i, o) {
    if (o.length < vs) {
      var a;
      return (a = this.values_).splice.apply(a, [r, i].concat(o));
    } else {
      var s = this.values_.slice(r, r + i), l = this.values_.slice(r + i);
      this.values_.length += o.length - i;
      for (var c = 0; c < o.length; c++)
        this.values_[r + c] = o[c];
      for (var u = 0; u < l.length; u++)
        this.values_[r + o.length + u] = l[u];
      return s;
    }
  }, t.notifyArrayChildUpdate_ = function(r, i, o) {
    var a = !this.owned_ && C(), s = K(this), l = s || a ? {
      observableKind: "array",
      object: this.proxy_,
      type: F,
      debugObjectName: this.atom_.name_,
      index: r,
      newValue: i,
      oldValue: o
    } : null;
    process.env.NODE_ENV !== "production" && a && k(l), this.atom_.reportChanged(), s && q(this, l), process.env.NODE_ENV !== "production" && a && I();
  }, t.notifyArraySplice_ = function(r, i, o) {
    var a = !this.owned_ && C(), s = K(this), l = s || a ? {
      observableKind: "array",
      object: this.proxy_,
      debugObjectName: this.atom_.name_,
      type: hr,
      index: r,
      removed: o,
      added: i,
      removedCount: o.length,
      addedCount: i.length
    } : null;
    process.env.NODE_ENV !== "production" && a && k(l), this.atom_.reportChanged(), s && q(this, l), process.env.NODE_ENV !== "production" && a && I();
  }, t.get_ = function(r) {
    if (this.legacyMode_ && r >= this.values_.length) {
      console.warn(process.env.NODE_ENV !== "production" ? "[mobx.array] Attempt to read an array index (" + r + ") that is out of bounds (" + this.values_.length + "). Please check length first. Out of bound indices will not be tracked by MobX" : "[mobx] Out of bounds read: " + r);
      return;
    }
    return this.atom_.reportObserved(), this.dehanceValue_(this.values_[r]);
  }, t.set_ = function(r, i) {
    var o = this.values_;
    if (this.legacyMode_ && r > o.length && h(17, r, o.length), r < o.length) {
      Y(this.atom_);
      var a = o[r];
      if (R(this)) {
        var s = j(this, {
          type: F,
          object: this.proxy_,
          // since "this" is the real array we need to pass its proxy
          index: r,
          newValue: i
        });
        if (!s)
          return;
        i = s.newValue;
      }
      i = this.enhancer_(i, a);
      var l = i !== a;
      l && (o[r] = i, this.notifyArrayChildUpdate_(r, i, a));
    } else {
      for (var c = new Array(r + 1 - o.length), u = 0; u < c.length - 1; u++)
        c[u] = void 0;
      c[c.length - 1] = i, this.spliceWithArray_(o.length, 0, c);
    }
  }, e;
}();
function gs(e, t, n, r) {
  return n === void 0 && (n = process.env.NODE_ENV !== "production" ? "ObservableArray@" + B() : "ObservableArray"), r === void 0 && (r = !1), oi(), Ie(function() {
    var i = new Qn(n, t, r, !1);
    si(i.values_, m, i);
    var o = new Proxy(i.values_, ps);
    return i.proxy_ = o, e && e.length && i.spliceWithArray_(0, 0, e), o;
  });
}
var Wt = {
  clear: function() {
    return this.splice(0);
  },
  replace: function(t) {
    var n = this[m];
    return n.spliceWithArray_(0, n.values_.length, t);
  },
  // Used by JSON.stringify
  toJSON: function() {
    return this.slice();
  },
  /*
   * functions that do alter the internal structure of the array, (based on lib.es6.d.ts)
   * since these functions alter the inner structure of the array, the have side effects.
   * Because the have side effects, they should not be used in computed function,
   * and for that reason the do not call dependencyState.notifyObserved
   */
  splice: function(t, n) {
    for (var r = arguments.length, i = new Array(r > 2 ? r - 2 : 0), o = 2; o < r; o++)
      i[o - 2] = arguments[o];
    var a = this[m];
    switch (arguments.length) {
      case 0:
        return [];
      case 1:
        return a.spliceWithArray_(t);
      case 2:
        return a.spliceWithArray_(t, n);
    }
    return a.spliceWithArray_(t, n, i);
  },
  spliceWithArray: function(t, n, r) {
    return this[m].spliceWithArray_(t, n, r);
  },
  push: function() {
    for (var t = this[m], n = arguments.length, r = new Array(n), i = 0; i < n; i++)
      r[i] = arguments[i];
    return t.spliceWithArray_(t.values_.length, 0, r), t.values_.length;
  },
  pop: function() {
    return this.splice(Math.max(this[m].values_.length - 1, 0), 1)[0];
  },
  shift: function() {
    return this.splice(0, 1)[0];
  },
  unshift: function() {
    for (var t = this[m], n = arguments.length, r = new Array(n), i = 0; i < n; i++)
      r[i] = arguments[i];
    return t.spliceWithArray_(0, 0, r), t.values_.length;
  },
  reverse: function() {
    return f.trackingDerivation && h(37, "reverse"), this.replace(this.slice().reverse()), this;
  },
  sort: function() {
    f.trackingDerivation && h(37, "sort");
    var t = this.slice();
    return t.sort.apply(t, arguments), this.replace(t), this;
  },
  remove: function(t) {
    var n = this[m], r = n.dehanceValues_(n.values_).indexOf(t);
    return r > -1 ? (this.splice(r, 1), !0) : !1;
  }
};
w("at", V);
w("concat", V);
w("flat", V);
w("includes", V);
w("indexOf", V);
w("join", V);
w("lastIndexOf", V);
w("slice", V);
w("toString", V);
w("toLocaleString", V);
w("toSorted", V);
w("toSpliced", V);
w("with", V);
w("every", W);
w("filter", W);
w("find", W);
w("findIndex", W);
w("findLast", W);
w("findLastIndex", W);
w("flatMap", W);
w("forEach", W);
w("map", W);
w("some", W);
w("toReversed", W);
w("reduce", Ui);
w("reduceRight", Ui);
function w(e, t) {
  typeof Array.prototype[e] == "function" && (Wt[e] = t(e));
}
function V(e) {
  return function() {
    var t = this[m];
    t.atom_.reportObserved();
    var n = t.dehanceValues_(t.values_);
    return n[e].apply(n, arguments);
  };
}
function W(e) {
  return function(t, n) {
    var r = this, i = this[m];
    i.atom_.reportObserved();
    var o = i.dehanceValues_(i.values_);
    return o[e](function(a, s) {
      return t.call(n, a, s, r);
    });
  };
}
function Ui(e) {
  return function() {
    var t = this, n = this[m];
    n.atom_.reportObserved();
    var r = n.dehanceValues_(n.values_), i = arguments[0];
    return arguments[0] = function(o, a, s) {
      return i(o, a, s, t);
    }, r[e].apply(r, arguments);
  };
}
var ms = /* @__PURE__ */ Te("ObservableArrayAdministration", Qn);
function fn(e) {
  return nn(e) && ms(e[m]);
}
var bs = {}, de = "add", Gt = "delete", Bi = /* @__PURE__ */ function() {
  function e(n, r, i) {
    var o = this;
    r === void 0 && (r = Ne), i === void 0 && (i = process.env.NODE_ENV !== "production" ? "ObservableMap@" + B() : "ObservableMap"), this.enhancer_ = void 0, this.name_ = void 0, this[m] = bs, this.data_ = void 0, this.hasMap_ = void 0, this.keysAtom_ = void 0, this.interceptors_ = void 0, this.changeListeners_ = void 0, this.dehancer = void 0, this.enhancer_ = r, this.name_ = i, A(Map) || h(18), Ie(function() {
      o.keysAtom_ = di(process.env.NODE_ENV !== "production" ? o.name_ + ".keys()" : "ObservableMap.keys()"), o.data_ = /* @__PURE__ */ new Map(), o.hasMap_ = /* @__PURE__ */ new Map(), n && o.merge(n);
    });
  }
  var t = e.prototype;
  return t.has_ = function(r) {
    return this.data_.has(r);
  }, t.has = function(r) {
    var i = this;
    if (!f.trackingDerivation)
      return this.has_(r);
    var o = this.hasMap_.get(r);
    if (!o) {
      var a = o = new Se(this.has_(r), an, process.env.NODE_ENV !== "production" ? this.name_ + "." + Pn(r) + "?" : "ObservableMap.key?", !1);
      this.hasMap_.set(r, a), Ri(a, function() {
        return i.hasMap_.delete(r);
      });
    }
    return o.get();
  }, t.set = function(r, i) {
    var o = this.has_(r);
    if (R(this)) {
      var a = j(this, {
        type: o ? F : de,
        object: this,
        newValue: i,
        name: r
      });
      if (!a)
        return this;
      i = a.newValue;
    }
    return o ? this.updateValue_(r, i) : this.addValue_(r, i), this;
  }, t.delete = function(r) {
    var i = this;
    if (Y(this.keysAtom_), R(this)) {
      var o = j(this, {
        type: Gt,
        object: this,
        name: r
      });
      if (!o)
        return !1;
    }
    if (this.has_(r)) {
      var a = C(), s = K(this), l = s || a ? {
        observableKind: "map",
        debugObjectName: this.name_,
        type: Gt,
        object: this,
        oldValue: this.data_.get(r).value_,
        name: r
      } : null;
      return process.env.NODE_ENV !== "production" && a && k(l), oe(function() {
        var c;
        i.keysAtom_.reportChanged(), (c = i.hasMap_.get(r)) == null || c.setNewValue_(!1);
        var u = i.data_.get(r);
        u.setNewValue_(void 0), i.data_.delete(r);
      }), s && q(this, l), process.env.NODE_ENV !== "production" && a && I(), !0;
    }
    return !1;
  }, t.updateValue_ = function(r, i) {
    var o = this.data_.get(r);
    if (i = o.prepareNewValue_(i), i !== f.UNCHANGED) {
      var a = C(), s = K(this), l = s || a ? {
        observableKind: "map",
        debugObjectName: this.name_,
        type: F,
        object: this,
        oldValue: o.value_,
        name: r,
        newValue: i
      } : null;
      process.env.NODE_ENV !== "production" && a && k(l), o.setNewValue_(i), s && q(this, l), process.env.NODE_ENV !== "production" && a && I();
    }
  }, t.addValue_ = function(r, i) {
    var o = this;
    Y(this.keysAtom_), oe(function() {
      var c, u = new Se(i, o.enhancer_, process.env.NODE_ENV !== "production" ? o.name_ + "." + Pn(r) : "ObservableMap.key", !1);
      o.data_.set(r, u), i = u.value_, (c = o.hasMap_.get(r)) == null || c.setNewValue_(!0), o.keysAtom_.reportChanged();
    });
    var a = C(), s = K(this), l = s || a ? {
      observableKind: "map",
      debugObjectName: this.name_,
      type: de,
      object: this,
      name: r,
      newValue: i
    } : null;
    process.env.NODE_ENV !== "production" && a && k(l), s && q(this, l), process.env.NODE_ENV !== "production" && a && I();
  }, t.get = function(r) {
    return this.has(r) ? this.dehanceValue_(this.data_.get(r).get()) : this.dehanceValue_(void 0);
  }, t.dehanceValue_ = function(r) {
    return this.dehancer !== void 0 ? this.dehancer(r) : r;
  }, t.keys = function() {
    return this.keysAtom_.reportObserved(), this.data_.keys();
  }, t.values = function() {
    var r = this, i = this.keys();
    return vr({
      next: function() {
        var a = i.next(), s = a.done, l = a.value;
        return {
          done: s,
          value: s ? void 0 : r.get(l)
        };
      }
    });
  }, t.entries = function() {
    var r = this, i = this.keys();
    return vr({
      next: function() {
        var a = i.next(), s = a.done, l = a.value;
        return {
          done: s,
          value: s ? void 0 : [l, r.get(l)]
        };
      }
    });
  }, t[Symbol.iterator] = function() {
    return this.entries();
  }, t.forEach = function(r, i) {
    for (var o = ze(this), a; !(a = o()).done; ) {
      var s = a.value, l = s[0], c = s[1];
      r.call(i, c, l, this);
    }
  }, t.merge = function(r) {
    var i = this;
    return me(r) && (r = new Map(r)), oe(function() {
      P(r) ? Bo(r).forEach(function(o) {
        return i.set(o, r[o]);
      }) : Array.isArray(r) ? r.forEach(function(o) {
        var a = o[0], s = o[1];
        return i.set(a, s);
      }) : Ge(r) ? (Uo(r) || h(19, r), r.forEach(function(o, a) {
        return i.set(a, o);
      })) : r != null && h(20, r);
    }), this;
  }, t.clear = function() {
    var r = this;
    oe(function() {
      Ei(function() {
        for (var i = ze(r.keys()), o; !(o = i()).done; ) {
          var a = o.value;
          r.delete(a);
        }
      });
    });
  }, t.replace = function(r) {
    var i = this;
    return oe(function() {
      for (var o = _s(r), a = /* @__PURE__ */ new Map(), s = !1, l = ze(i.data_.keys()), c; !(c = l()).done; ) {
        var u = c.value;
        if (!o.has(u)) {
          var d = i.delete(u);
          if (d)
            s = !0;
          else {
            var v = i.data_.get(u);
            a.set(u, v);
          }
        }
      }
      for (var p = ze(o.entries()), b; !(b = p()).done; ) {
        var E = b.value, x = E[0], G = E[1], Re = i.data_.has(x);
        if (i.set(x, G), i.data_.has(x)) {
          var et = i.data_.get(x);
          a.set(x, et), Re || (s = !0);
        }
      }
      if (!s)
        if (i.data_.size !== a.size)
          i.keysAtom_.reportChanged();
        else
          for (var $ = i.data_.keys(), ce = a.keys(), be = $.next(), ar = ce.next(); !be.done; ) {
            if (be.value !== ar.value) {
              i.keysAtom_.reportChanged();
              break;
            }
            be = $.next(), ar = ce.next();
          }
      i.data_ = a;
    }), this;
  }, t.toString = function() {
    return "[object ObservableMap]";
  }, t.toJSON = function() {
    return Array.from(this);
  }, t.observe_ = function(r, i) {
    return process.env.NODE_ENV !== "production" && i === !0 && h("`observe` doesn't support fireImmediately=true in combination with maps."), Nt(this, r);
  }, t.intercept_ = function(r) {
    return xt(this, r);
  }, Je(e, [{
    key: "size",
    get: function() {
      return this.keysAtom_.reportObserved(), this.data_.size;
    }
  }, {
    key: Symbol.toStringTag,
    get: function() {
      return "Map";
    }
  }]);
}(), me = /* @__PURE__ */ Te("ObservableMap", Bi);
function vr(e) {
  return e[Symbol.toStringTag] = "MapIterator", tr(e);
}
function _s(e) {
  if (Ge(e) || me(e))
    return e;
  if (Array.isArray(e))
    return new Map(e);
  if (P(e)) {
    var t = /* @__PURE__ */ new Map();
    for (var n in e)
      t.set(n, e[n]);
    return t;
  } else
    return h(21, e);
}
var ys = {}, Fi = /* @__PURE__ */ function() {
  function e(n, r, i) {
    var o = this;
    r === void 0 && (r = Ne), i === void 0 && (i = process.env.NODE_ENV !== "production" ? "ObservableSet@" + B() : "ObservableSet"), this.name_ = void 0, this[m] = ys, this.data_ = /* @__PURE__ */ new Set(), this.atom_ = void 0, this.changeListeners_ = void 0, this.interceptors_ = void 0, this.dehancer = void 0, this.enhancer_ = void 0, this.name_ = i, A(Set) || h(22), this.enhancer_ = function(a, s) {
      return r(a, s, i);
    }, Ie(function() {
      o.atom_ = di(o.name_), n && o.replace(n);
    });
  }
  var t = e.prototype;
  return t.dehanceValue_ = function(r) {
    return this.dehancer !== void 0 ? this.dehancer(r) : r;
  }, t.clear = function() {
    var r = this;
    oe(function() {
      Ei(function() {
        for (var i = ze(r.data_.values()), o; !(o = i()).done; ) {
          var a = o.value;
          r.delete(a);
        }
      });
    });
  }, t.forEach = function(r, i) {
    for (var o = ze(this), a; !(a = o()).done; ) {
      var s = a.value;
      r.call(i, s, s, this);
    }
  }, t.add = function(r) {
    var i = this;
    if (Y(this.atom_), R(this)) {
      var o = j(this, {
        type: de,
        object: this,
        newValue: r
      });
      if (!o)
        return this;
      r = o.newValue;
    }
    if (!this.has(r)) {
      oe(function() {
        i.data_.add(i.enhancer_(r, void 0)), i.atom_.reportChanged();
      });
      var a = process.env.NODE_ENV !== "production" && C(), s = K(this), l = s || a ? {
        observableKind: "set",
        debugObjectName: this.name_,
        type: de,
        object: this,
        newValue: r
      } : null;
      a && process.env.NODE_ENV !== "production" && k(l), s && q(this, l), a && process.env.NODE_ENV !== "production" && I();
    }
    return this;
  }, t.delete = function(r) {
    var i = this;
    if (R(this)) {
      var o = j(this, {
        type: Gt,
        object: this,
        oldValue: r
      });
      if (!o)
        return !1;
    }
    if (this.has(r)) {
      var a = process.env.NODE_ENV !== "production" && C(), s = K(this), l = s || a ? {
        observableKind: "set",
        debugObjectName: this.name_,
        type: Gt,
        object: this,
        oldValue: r
      } : null;
      return a && process.env.NODE_ENV !== "production" && k(l), oe(function() {
        i.atom_.reportChanged(), i.data_.delete(r);
      }), s && q(this, l), a && process.env.NODE_ENV !== "production" && I(), !0;
    }
    return !1;
  }, t.has = function(r) {
    return this.atom_.reportObserved(), this.data_.has(this.dehanceValue_(r));
  }, t.entries = function() {
    var r = this.values();
    return pr({
      next: function() {
        var o = r.next(), a = o.value, s = o.done;
        return s ? {
          value: void 0,
          done: s
        } : {
          value: [a, a],
          done: s
        };
      }
    });
  }, t.keys = function() {
    return this.values();
  }, t.values = function() {
    this.atom_.reportObserved();
    var r = this, i = this.data_.values();
    return pr({
      next: function() {
        var a = i.next(), s = a.value, l = a.done;
        return l ? {
          value: void 0,
          done: l
        } : {
          value: r.dehanceValue_(s),
          done: l
        };
      }
    });
  }, t.intersection = function(r) {
    if (re(r) && !X(r))
      return r.intersection(this);
    var i = new Set(this);
    return i.intersection(r);
  }, t.union = function(r) {
    if (re(r) && !X(r))
      return r.union(this);
    var i = new Set(this);
    return i.union(r);
  }, t.difference = function(r) {
    return new Set(this).difference(r);
  }, t.symmetricDifference = function(r) {
    if (re(r) && !X(r))
      return r.symmetricDifference(this);
    var i = new Set(this);
    return i.symmetricDifference(r);
  }, t.isSubsetOf = function(r) {
    return new Set(this).isSubsetOf(r);
  }, t.isSupersetOf = function(r) {
    return new Set(this).isSupersetOf(r);
  }, t.isDisjointFrom = function(r) {
    if (re(r) && !X(r))
      return r.isDisjointFrom(this);
    var i = new Set(this);
    return i.isDisjointFrom(r);
  }, t.replace = function(r) {
    var i = this;
    return X(r) && (r = new Set(r)), oe(function() {
      Array.isArray(r) ? (i.clear(), r.forEach(function(o) {
        return i.add(o);
      })) : re(r) ? (i.clear(), r.forEach(function(o) {
        return i.add(o);
      })) : r != null && h("Cannot initialize set from " + r);
    }), this;
  }, t.observe_ = function(r, i) {
    return process.env.NODE_ENV !== "production" && i === !0 && h("`observe` doesn't support fireImmediately=true in combination with sets."), Nt(this, r);
  }, t.intercept_ = function(r) {
    return xt(this, r);
  }, t.toJSON = function() {
    return Array.from(this);
  }, t.toString = function() {
    return "[object ObservableSet]";
  }, t[Symbol.iterator] = function() {
    return this.values();
  }, Je(e, [{
    key: "size",
    get: function() {
      return this.atom_.reportObserved(), this.data_.size;
    }
  }, {
    key: Symbol.toStringTag,
    get: function() {
      return "Set";
    }
  }]);
}(), X = /* @__PURE__ */ Te("ObservableSet", Fi);
function pr(e) {
  return e[Symbol.toStringTag] = "SetIterator", tr(e);
}
var gr = /* @__PURE__ */ Object.create(null), mr = "remove", jn = /* @__PURE__ */ function() {
  function e(n, r, i, o) {
    r === void 0 && (r = /* @__PURE__ */ new Map()), o === void 0 && (o = ma), this.target_ = void 0, this.values_ = void 0, this.name_ = void 0, this.defaultAnnotation_ = void 0, this.keysAtom_ = void 0, this.changeListeners_ = void 0, this.interceptors_ = void 0, this.proxy_ = void 0, this.isPlainObject_ = void 0, this.appliedAnnotations_ = void 0, this.pendingKeys_ = void 0, this.target_ = n, this.values_ = r, this.name_ = i, this.defaultAnnotation_ = o, this.keysAtom_ = new ge(process.env.NODE_ENV !== "production" ? this.name_ + ".keys" : "ObservableObject.keys"), this.isPlainObject_ = P(this.target_), process.env.NODE_ENV !== "production" && !Ji(this.defaultAnnotation_) && h("defaultAnnotation must be valid annotation"), process.env.NODE_ENV !== "production" && (this.appliedAnnotations_ = {});
  }
  var t = e.prototype;
  return t.getObservablePropValue_ = function(r) {
    return this.values_.get(r).get();
  }, t.setObservablePropValue_ = function(r, i) {
    var o = this.values_.get(r);
    if (o instanceof U)
      return o.set(i), !0;
    if (R(this)) {
      var a = j(this, {
        type: F,
        object: this.proxy_ || this.target_,
        name: r,
        newValue: i
      });
      if (!a)
        return null;
      i = a.newValue;
    }
    if (i = o.prepareNewValue_(i), i !== f.UNCHANGED) {
      var s = K(this), l = process.env.NODE_ENV !== "production" && C(), c = s || l ? {
        type: F,
        observableKind: "object",
        debugObjectName: this.name_,
        object: this.proxy_ || this.target_,
        oldValue: o.value_,
        name: r,
        newValue: i
      } : null;
      process.env.NODE_ENV !== "production" && l && k(c), o.setNewValue_(i), s && q(this, c), process.env.NODE_ENV !== "production" && l && I();
    }
    return !0;
  }, t.get_ = function(r) {
    return f.trackingDerivation && !H(this.target_, r) && this.has_(r), this.target_[r];
  }, t.set_ = function(r, i, o) {
    return o === void 0 && (o = !1), H(this.target_, r) ? this.values_.has(r) ? this.setObservablePropValue_(r, i) : o ? Reflect.set(this.target_, r, i) : (this.target_[r] = i, !0) : this.extend_(r, {
      value: i,
      enumerable: !0,
      writable: !0,
      configurable: !0
    }, this.defaultAnnotation_, o);
  }, t.has_ = function(r) {
    if (!f.trackingDerivation)
      return r in this.target_;
    this.pendingKeys_ || (this.pendingKeys_ = /* @__PURE__ */ new Map());
    var i = this.pendingKeys_.get(r);
    return i || (i = new Se(r in this.target_, an, process.env.NODE_ENV !== "production" ? this.name_ + "." + Pn(r) + "?" : "ObservableObject.key?", !1), this.pendingKeys_.set(r, i)), i.get();
  }, t.make_ = function(r, i) {
    if (i === !0 && (i = this.defaultAnnotation_), i !== !1) {
      if (yr(this, i, r), !(r in this.target_)) {
        var o;
        if ((o = this.target_[ie]) != null && o[r])
          return;
        h(1, i.annotationType_, this.name_ + "." + r.toString());
      }
      for (var a = this.target_; a && a !== tn; ) {
        var s = Ut(a, r);
        if (s) {
          var l = i.make_(this, r, s, a);
          if (l === 0)
            return;
          if (l === 1)
            break;
        }
        a = Object.getPrototypeOf(a);
      }
      _r(this, i, r);
    }
  }, t.extend_ = function(r, i, o, a) {
    if (a === void 0 && (a = !1), o === !0 && (o = this.defaultAnnotation_), o === !1)
      return this.defineProperty_(r, i, a);
    yr(this, o, r);
    var s = o.extend_(this, r, i, a);
    return s && _r(this, o, r), s;
  }, t.defineProperty_ = function(r, i, o) {
    o === void 0 && (o = !1), Y(this.keysAtom_);
    try {
      M();
      var a = this.delete_(r);
      if (!a)
        return a;
      if (R(this)) {
        var s = j(this, {
          object: this.proxy_ || this.target_,
          name: r,
          type: de,
          newValue: i.value
        });
        if (!s)
          return null;
        var l = s.newValue;
        i.value !== l && (i = he({}, i, {
          value: l
        }));
      }
      if (o) {
        if (!Reflect.defineProperty(this.target_, r, i))
          return !1;
      } else
        Z(this.target_, r, i);
      this.notifyPropertyAddition_(r, i.value);
    } finally {
      L();
    }
    return !0;
  }, t.defineObservableProperty_ = function(r, i, o, a) {
    a === void 0 && (a = !1), Y(this.keysAtom_);
    try {
      M();
      var s = this.delete_(r);
      if (!s)
        return s;
      if (R(this)) {
        var l = j(this, {
          object: this.proxy_ || this.target_,
          name: r,
          type: de,
          newValue: i
        });
        if (!l)
          return null;
        i = l.newValue;
      }
      var c = br(r), u = {
        configurable: f.safeDescriptors ? this.isPlainObject_ : !0,
        enumerable: !0,
        get: c.get,
        set: c.set
      };
      if (a) {
        if (!Reflect.defineProperty(this.target_, r, u))
          return !1;
      } else
        Z(this.target_, r, u);
      var d = new Se(i, o, process.env.NODE_ENV !== "production" ? this.name_ + "." + r.toString() : "ObservableObject.key", !1);
      this.values_.set(r, d), this.notifyPropertyAddition_(r, d.value_);
    } finally {
      L();
    }
    return !0;
  }, t.defineComputedProperty_ = function(r, i, o) {
    o === void 0 && (o = !1), Y(this.keysAtom_);
    try {
      M();
      var a = this.delete_(r);
      if (!a)
        return a;
      if (R(this)) {
        var s = j(this, {
          object: this.proxy_ || this.target_,
          name: r,
          type: de,
          newValue: void 0
        });
        if (!s)
          return null;
      }
      i.name || (i.name = process.env.NODE_ENV !== "production" ? this.name_ + "." + r.toString() : "ObservableObject.key"), i.context = this.proxy_ || this.target_;
      var l = br(r), c = {
        configurable: f.safeDescriptors ? this.isPlainObject_ : !0,
        enumerable: !1,
        get: l.get,
        set: l.set
      };
      if (o) {
        if (!Reflect.defineProperty(this.target_, r, c))
          return !1;
      } else
        Z(this.target_, r, c);
      this.values_.set(r, new U(i)), this.notifyPropertyAddition_(r, void 0);
    } finally {
      L();
    }
    return !0;
  }, t.delete_ = function(r, i) {
    if (i === void 0 && (i = !1), Y(this.keysAtom_), !H(this.target_, r))
      return !0;
    if (R(this)) {
      var o = j(this, {
        object: this.proxy_ || this.target_,
        name: r,
        type: mr
      });
      if (!o)
        return null;
    }
    try {
      var a;
      M();
      var s = K(this), l = process.env.NODE_ENV !== "production" && C(), c = this.values_.get(r), u = void 0;
      if (!c && (s || l)) {
        var d;
        u = (d = Ut(this.target_, r)) == null ? void 0 : d.value;
      }
      if (i) {
        if (!Reflect.deleteProperty(this.target_, r))
          return !1;
      } else
        delete this.target_[r];
      if (process.env.NODE_ENV !== "production" && delete this.appliedAnnotations_[r], c && (this.values_.delete(r), c instanceof Se && (u = c.value_), Ni(c)), this.keysAtom_.reportChanged(), (a = this.pendingKeys_) == null || (a = a.get(r)) == null || a.set(r in this.target_), s || l) {
        var v = {
          type: mr,
          observableKind: "object",
          object: this.proxy_ || this.target_,
          debugObjectName: this.name_,
          oldValue: u,
          name: r
        };
        process.env.NODE_ENV !== "production" && l && k(v), s && q(this, v), process.env.NODE_ENV !== "production" && l && I();
      }
    } finally {
      L();
    }
    return !0;
  }, t.observe_ = function(r, i) {
    return process.env.NODE_ENV !== "production" && i === !0 && h("`observe` doesn't support the fire immediately property for observable objects."), Nt(this, r);
  }, t.intercept_ = function(r) {
    return xt(this, r);
  }, t.notifyPropertyAddition_ = function(r, i) {
    var o, a = K(this), s = process.env.NODE_ENV !== "production" && C();
    if (a || s) {
      var l = a || s ? {
        type: de,
        observableKind: "object",
        debugObjectName: this.name_,
        object: this.proxy_ || this.target_,
        name: r,
        newValue: i
      } : null;
      process.env.NODE_ENV !== "production" && s && k(l), a && q(this, l), process.env.NODE_ENV !== "production" && s && I();
    }
    (o = this.pendingKeys_) == null || (o = o.get(r)) == null || o.set(!0), this.keysAtom_.reportChanged();
  }, t.ownKeys_ = function() {
    return this.keysAtom_.reportObserved(), pt(this.target_);
  }, t.keys_ = function() {
    return this.keysAtom_.reportObserved(), Object.keys(this.target_);
  }, e;
}();
function Ye(e, t) {
  var n;
  if (process.env.NODE_ENV !== "production" && t && Ze(e) && h("Options can't be provided for already observable objects."), H(e, m))
    return process.env.NODE_ENV !== "production" && !(Wi(e) instanceof jn) && h("Cannot convert '" + Jt(e) + `' into observable object:
The target is already observable of different type.
Extending builtins is not supported.`), e;
  process.env.NODE_ENV !== "production" && !Object.isExtensible(e) && h("Cannot make the designated object observable; it is not extensible");
  var r = (n = t?.name) != null ? n : process.env.NODE_ENV !== "production" ? (P(e) ? "ObservableObject" : e.constructor.name) + "@" + B() : "ObservableObject", i = new jn(e, /* @__PURE__ */ new Map(), String(r), Ca(t));
  return rn(e, m, i), e;
}
var ws = /* @__PURE__ */ Te("ObservableObjectAdministration", jn);
function br(e) {
  return gr[e] || (gr[e] = {
    get: function() {
      return this[m].getObservablePropValue_(e);
    },
    set: function(n) {
      return this[m].setObservablePropValue_(e, n);
    }
  });
}
function Ze(e) {
  return nn(e) ? ws(e[m]) : !1;
}
function _r(e, t, n) {
  var r;
  process.env.NODE_ENV !== "production" && (e.appliedAnnotations_[n] = t), (r = e.target_[ie]) == null || delete r[n];
}
function yr(e, t, n) {
  if (process.env.NODE_ENV !== "production" && !Ji(t) && h("Cannot annotate '" + e.name_ + "." + n.toString() + "': Invalid annotation."), process.env.NODE_ENV !== "production" && !Ft(t) && H(e.appliedAnnotations_, n)) {
    var r = e.name_ + "." + n.toString(), i = e.appliedAnnotations_[n].annotationType_, o = t.annotationType_;
    h("Cannot apply '" + o + "' to '" + r + "':" + (`
The field is already annotated with '` + i + "'.") + `
Re-annotating fields is not allowed.
Use 'override' annotation for methods overridden by subclass.`);
  }
}
var Es = /* @__PURE__ */ Ki(0), Os = /* @__PURE__ */ function() {
  var e = !1, t = {};
  return Object.defineProperty(t, "0", {
    set: function() {
      e = !0;
    }
  }), Object.create(t)[0] = 1, e === !1;
}(), wn = 0, Hi = function() {
};
function As(e, t) {
  Object.setPrototypeOf ? Object.setPrototypeOf(e.prototype, t) : e.prototype.__proto__ !== void 0 ? e.prototype.__proto__ = t : e.prototype = t;
}
As(Hi, Array.prototype);
var er = /* @__PURE__ */ function(e) {
  function t(r, i, o, a) {
    var s;
    return o === void 0 && (o = process.env.NODE_ENV !== "production" ? "ObservableArray@" + B() : "ObservableArray"), a === void 0 && (a = !1), s = e.call(this) || this, Ie(function() {
      var l = new Qn(o, i, a, !0);
      l.proxy_ = s, si(s, m, l), r && r.length && s.spliceWithArray(0, 0, r), Os && Object.defineProperty(s, "0", Es);
    }), s;
  }
  ui(t, e);
  var n = t.prototype;
  return n.concat = function() {
    this[m].atom_.reportObserved();
    for (var i = arguments.length, o = new Array(i), a = 0; a < i; a++)
      o[a] = arguments[a];
    return Array.prototype.concat.apply(
      this.slice(),
      //@ts-ignore
      o.map(function(s) {
        return fn(s) ? s.slice() : s;
      })
    );
  }, n[Symbol.iterator] = function() {
    var r = this, i = 0;
    return tr({
      next: function() {
        return i < r.length ? {
          value: r[i++],
          done: !1
        } : {
          done: !0,
          value: void 0
        };
      }
    });
  }, Je(t, [{
    key: "length",
    get: function() {
      return this[m].getArrayLength_();
    },
    set: function(i) {
      this[m].setArrayLength_(i);
    }
  }, {
    key: Symbol.toStringTag,
    get: function() {
      return "Array";
    }
  }]);
}(Hi);
Object.entries(Wt).forEach(function(e) {
  var t = e[0], n = e[1];
  t !== "concat" && rn(er.prototype, t, n);
});
function Ki(e) {
  return {
    enumerable: !1,
    configurable: !0,
    get: function() {
      return this[m].get_(e);
    },
    set: function(n) {
      this[m].set_(e, n);
    }
  };
}
function Ss(e) {
  Z(er.prototype, "" + e, Ki(e));
}
function qi(e) {
  if (e > wn) {
    for (var t = wn; t < e + 100; t++)
      Ss(t);
    wn = e;
  }
}
qi(1e3);
function xs(e, t, n) {
  return new er(e, t, n);
}
function Ke(e, t) {
  if (typeof e == "object" && e !== null) {
    if (fn(e))
      return t !== void 0 && h(23), e[m].atom_;
    if (X(e))
      return e.atom_;
    if (me(e)) {
      if (t === void 0)
        return e.keysAtom_;
      var n = e.data_.get(t) || e.hasMap_.get(t);
      return n || h(25, t, Jt(e)), n;
    }
    if (Ze(e)) {
      if (!t)
        return h(26);
      var r = e[m].values_.get(t);
      return r || h(27, t, Jt(e)), r;
    }
    if (Gn(e) || dn(e) || qt(e))
      return e;
  } else if (A(e) && qt(e[m]))
    return e[m];
  h(28);
}
function Wi(e, t) {
  if (e || h(29), Gn(e) || dn(e) || qt(e) || me(e) || X(e))
    return e;
  if (e[m])
    return e[m];
  h(24, e);
}
function Jt(e, t) {
  var n;
  if (t !== void 0)
    n = Ke(e, t);
  else {
    if (Fe(e))
      return e.name;
    Ze(e) || me(e) || X(e) ? n = Wi(e) : n = Ke(e);
  }
  return n.name_;
}
function Ie(e) {
  var t = ke(), n = cn(!0);
  M();
  try {
    return e();
  } finally {
    L(), un(n), ae(t);
  }
}
var wr = tn.toString;
function Gi(e, t, n) {
  return n === void 0 && (n = -1), Mn(e, t, n);
}
function Mn(e, t, n, r, i) {
  if (e === t)
    return e !== 0 || 1 / e === 1 / t;
  if (e == null || t == null)
    return !1;
  if (e !== e)
    return t !== t;
  var o = typeof e;
  if (o !== "function" && o !== "object" && typeof t != "object")
    return !1;
  var a = wr.call(e);
  if (a !== wr.call(t))
    return !1;
  switch (a) {
    // Strings, numbers, regular expressions, dates, and booleans are compared by value.
    case "[object RegExp]":
    // RegExps are coerced to strings for comparison (Note: '' + /a/i === '/a/i')
    case "[object String]":
      return "" + e == "" + t;
    case "[object Number]":
      return +e != +e ? +t != +t : +e == 0 ? 1 / +e === 1 / t : +e == +t;
    case "[object Date]":
    case "[object Boolean]":
      return +e == +t;
    case "[object Symbol]":
      return typeof Symbol < "u" && Symbol.valueOf.call(e) === Symbol.valueOf.call(t);
    case "[object Map]":
    case "[object Set]":
      n >= 0 && n++;
      break;
  }
  e = Er(e), t = Er(t);
  var s = a === "[object Array]";
  if (!s) {
    if (typeof e != "object" || typeof t != "object")
      return !1;
    var l = e.constructor, c = t.constructor;
    if (l !== c && !(A(l) && l instanceof l && A(c) && c instanceof c) && "constructor" in e && "constructor" in t)
      return !1;
  }
  if (n === 0)
    return !1;
  n < 0 && (n = -1), r = r || [], i = i || [];
  for (var u = r.length; u--; )
    if (r[u] === e)
      return i[u] === t;
  if (r.push(e), i.push(t), s) {
    if (u = e.length, u !== t.length)
      return !1;
    for (; u--; )
      if (!Mn(e[u], t[u], n - 1, r, i))
        return !1;
  } else {
    var d = Object.keys(e), v = d.length;
    if (Object.keys(t).length !== v)
      return !1;
    for (var p = 0; p < v; p++) {
      var b = d[p];
      if (!(H(t, b) && Mn(e[b], t[b], n - 1, r, i)))
        return !1;
    }
  }
  return r.pop(), i.pop(), !0;
}
function Er(e) {
  return fn(e) ? e.slice() : Ge(e) || me(e) || re(e) || X(e) ? Array.from(e.entries()) : e;
}
var Or, Ns = ((Or = Kn().Iterator) == null ? void 0 : Or.prototype) || {};
function tr(e) {
  return e[Symbol.iterator] = Cs, Object.assign(Object.create(Ns), e);
}
function Cs() {
  return this;
}
function Ji(e) {
  return (
    // Can be function
    e instanceof Object && typeof e.annotationType_ == "string" && A(e.make_) && A(e.extend_)
  );
}
["Symbol", "Map", "Set"].forEach(function(e) {
  var t = Kn();
  typeof t[e] > "u" && h("MobX requires global '" + e + "' to be available or polyfilled");
});
typeof __MOBX_DEVTOOLS_GLOBAL_HOOK__ == "object" && __MOBX_DEVTOOLS_GLOBAL_HOOK__.injectMobx({
  spy: Ja,
  extras: {
    getDebugName: Jt
  },
  $mobx: m
});
const Ar = "copilot-conf";
class fe {
  static get sessionConfiguration() {
    const t = sessionStorage.getItem(Ar);
    return t ? JSON.parse(t) : {};
  }
  static saveCopilotActivation(t) {
    const n = this.sessionConfiguration;
    n.active = t, this.persist(n);
  }
  static getCopilotActivation() {
    return this.sessionConfiguration.active;
  }
  static saveSpotlightActivation(t) {
    const n = this.sessionConfiguration;
    n.spotlightActive = t, this.persist(n);
  }
  static getSpotlightActivation() {
    return this.sessionConfiguration.spotlightActive;
  }
  static saveSpotlightPosition(t, n, r, i) {
    const o = this.sessionConfiguration;
    o.spotlightPosition = { left: t, top: n, right: r, bottom: i }, this.persist(o);
  }
  static getSpotlightPosition() {
    return this.sessionConfiguration.spotlightPosition;
  }
  static saveDrawerSize(t, n) {
    const r = this.sessionConfiguration;
    r.drawerSizes = r.drawerSizes ?? {}, r.drawerSizes[t] = n, this.persist(r);
  }
  static getDrawerSize(t) {
    const n = this.sessionConfiguration;
    if (n.drawerSizes)
      return n.drawerSizes[t];
  }
  static savePanelConfigurations(t) {
    const n = this.sessionConfiguration;
    n.sectionPanelState = t, this.persist(n);
  }
  static getPanelConfigurations() {
    return this.sessionConfiguration.sectionPanelState;
  }
  static persist(t) {
    sessionStorage.setItem(Ar, JSON.stringify(t));
  }
  static savePrompts(t) {
    const n = this.sessionConfiguration;
    n.prompts = t, this.persist(n);
  }
  static getPrompts() {
    return this.sessionConfiguration.prompts || [];
  }
  static saveCurrentSelection(t) {
    const n = this.sessionConfiguration;
    n.selection = n.selection ?? {}, n.selection && (n.selection.current = t, n.selection.location = window.location.pathname, this.persist(n));
  }
  static savePendingSelection(t) {
    const n = this.sessionConfiguration;
    n.selection = n.selection ?? {}, n.selection && (n.selection.pending = t, n.selection.location = window.location.pathname, this.persist(n));
  }
  static getCurrentSelection() {
    const t = this.sessionConfiguration.selection;
    if (t?.location === window.location.pathname)
      return t.current;
  }
  static getPendingSelection() {
    const t = this.sessionConfiguration.selection;
    if (t?.location === window.location.pathname)
      return t.pending;
  }
  static saveDrillDownContextReference(t) {
    const n = this.sessionConfiguration;
    n.drillDownContext = n.drillDownContext ?? {}, n.drillDownContext && (n.drillDownContext.location = window.location.pathname, n.drillDownContext.stack = t, this.persist(n));
  }
  static getDrillDownContextReference() {
    const t = this.sessionConfiguration;
    if (t?.drillDownContext?.location === window.location.pathname)
      return t.drillDownContext?.stack;
  }
}
var Ve = /* @__PURE__ */ ((e) => (e.INFORMATION = "information", e.WARNING = "warning", e.ERROR = "error", e))(Ve || {});
const Ps = Symbol.for("react.portal"), $s = Symbol.for("react.fragment"), Ds = Symbol.for("react.strict_mode"), Ts = Symbol.for("react.profiler"), ks = Symbol.for("react.provider"), Is = Symbol.for("react.context"), Xi = Symbol.for("react.forward_ref"), Vs = Symbol.for("react.suspense"), Rs = Symbol.for("react.suspense_list"), js = Symbol.for("react.memo"), Ms = Symbol.for("react.lazy");
function Ls(e, t, n) {
  const r = e.displayName;
  if (r)
    return r;
  const i = t.displayName || t.name || "";
  return i !== "" ? `${n}(${i})` : n;
}
function Sr(e) {
  return e.displayName || "Context";
}
function Xt(e) {
  if (e === null)
    return null;
  if (typeof e == "function")
    return e.displayName || e.name || null;
  if (typeof e == "string")
    return e;
  switch (e) {
    case $s:
      return "Fragment";
    case Ps:
      return "Portal";
    case Ts:
      return "Profiler";
    case Ds:
      return "StrictMode";
    case Vs:
      return "Suspense";
    case Rs:
      return "SuspenseList";
  }
  if (typeof e == "object")
    switch (e.$$typeof) {
      case Is:
        return `${Sr(e)}.Consumer`;
      case ks:
        return `${Sr(e._context)}.Provider`;
      case Xi:
        return Ls(e, e.render, "ForwardRef");
      case js:
        const t = e.displayName || null;
        return t !== null ? t : Xt(e.type) || "Memo";
      case Ms: {
        const n = e, r = n._payload, i = n._init;
        try {
          return Xt(i(r));
        } catch {
          return null;
        }
      }
    }
  return null;
}
let Dt;
function iu() {
  const e = /* @__PURE__ */ new Set();
  return Array.from(document.body.querySelectorAll("*")).flatMap(Bs).filter(zs).filter((n) => !n.fileName.endsWith("frontend/generated/flow/Flow.tsx")).forEach((n) => e.add(n.fileName)), Array.from(e);
}
function zs(e) {
  return !!e && e.fileName;
}
function Yt(e) {
  if (!e)
    return;
  if (e._debugSource)
    return e._debugSource;
  const t = e._debugInfo?.source;
  if (t?.fileName && t?.lineNumber)
    return t;
}
function Us(e) {
  if (e && e.type?.__debugSourceDefine)
    return e.type.__debugSourceDefine;
}
function Bs(e) {
  return Yt(Zt(e));
}
function Fs() {
  return `__reactFiber$${Yi()}`;
}
function Hs() {
  return `__reactContainer$${Yi()}`;
}
function Yi() {
  if (!(!Dt && (Dt = Array.from(document.querySelectorAll("*")).flatMap((e) => Object.keys(e)).filter((e) => e.startsWith("__reactFiber$")).map((e) => e.replace("__reactFiber$", "")).find((e) => e), !Dt)))
    return Dt;
}
function ct(e) {
  const t = e.type;
  return t?.$$typeof === Xi && !t.displayName && e.child ? ct(e.child) : Xt(e.type) ?? Xt(e.elementType) ?? "???";
}
function Ks() {
  const e = Array.from(document.querySelectorAll("body > *")).flatMap((n) => n[Hs()]).find((n) => n), t = $e(e);
  return $e(t?.child);
}
function qs(e) {
  const t = [];
  let n = $e(e.child);
  for (; n; )
    t.push(n), n = $e(n.sibling);
  return t;
}
function Ws(e) {
  return e.hasOwnProperty("entanglements") && e.hasOwnProperty("containerInfo");
}
function Gs(e) {
  return e.hasOwnProperty("stateNode") && e.hasOwnProperty("pendingProps");
}
function $e(e) {
  const t = e?.stateNode;
  if (t?.current && (Ws(t) || Gs(t)))
    return t?.current;
  if (!e)
    return;
  if (!e.alternate)
    return e;
  const n = e.alternate, r = e?.actualStartTime, i = n?.actualStartTime;
  return i !== r && i > r ? n : e;
}
function Zt(e) {
  const t = Fs(), n = $e(e[t]);
  if (Yt(n))
    return n;
  let r = n?.return || void 0;
  for (; r && !Yt(r); )
    r = r.return || void 0;
  return r;
}
function Qt(e) {
  if (e.stateNode?.isConnected === !0)
    return e.stateNode;
  if (e.child)
    return Qt(e.child);
}
function xr(e) {
  const t = Qt(e);
  return t && $e(Zt(t)) === e;
}
function Js(e) {
  return typeof e.type != "function" || Zi(e) ? !1 : !!(Yt(e) || Us(e));
}
function Zi(e) {
  if (!e)
    return !1;
  const t = e;
  return typeof e.type == "function" && t.tag === 1;
}
const hn = async (e, t, n) => window.Vaadin.copilot.comm(e, t, n), ve = "copilot-", Xs = "24.7.2", ou = "attention-required", au = "https://plugins.jetbrains.com/plugin/23758-vaadin", su = "https://marketplace.visualstudio.com/items?itemName=vaadin.vaadin-vscode";
function lu(e) {
  return e === void 0 ? !1 : e.nodeId >= 0;
}
function Ys(e) {
  if (e.javaClass)
    return e.javaClass.substring(e.javaClass.lastIndexOf(".") + 1);
}
function En(e) {
  const t = window.Vaadin;
  if (t && t.Flow) {
    const { clients: n } = t.Flow, r = Object.keys(n);
    for (const i of r) {
      const o = n[i];
      if (o.getNodeId) {
        const a = o.getNodeId(e);
        if (a >= 0) {
          const s = o.getNodeInfo(a);
          return {
            nodeId: a,
            uiId: o.getUIId(),
            element: e,
            javaClass: s.javaClass,
            styles: s.styles,
            hiddenByServer: s.hiddenByServer
          };
        }
      }
    }
  }
}
function cu() {
  const e = window.Vaadin;
  let t;
  if (e && e.Flow) {
    const { clients: n } = e.Flow, r = Object.keys(n);
    for (const i of r) {
      const o = n[i];
      o.getUIId && (t = o.getUIId());
    }
  }
  return t;
}
function uu(e) {
  return {
    uiId: e.uiId,
    nodeId: e.nodeId
  };
}
function Zs(e) {
  return e ? e.type?.type === "FlowContainer" : !1;
}
function Qs(e) {
  return e.localName.startsWith("flow-container");
}
function du(e) {
  const t = e.lastIndexOf(".");
  return t < 0 ? e : e.substring(t + 1);
}
function Qi(e, t) {
  const n = e();
  n ? t(n) : setTimeout(() => Qi(e, t), 50);
}
async function eo(e) {
  const t = e();
  if (t)
    return t;
  let n;
  const r = new Promise((o) => {
    n = o;
  }), i = setInterval(() => {
    const o = e();
    o && (clearInterval(i), n(o));
  }, 10);
  return r;
}
function to(e) {
  return S.box(e, { deep: !1 });
}
function el(e) {
  return e && typeof e.lastAccessedBy_ == "number";
}
function fu(e) {
  if (e) {
    if (typeof e == "string")
      return e;
    if (!el(e))
      throw new Error(`Expected message to be a string or an observable value but was ${JSON.stringify(e)}`);
    return e.get();
  }
}
function hu(e) {
  return Array.from(new Set(e));
}
function vn(e) {
  Promise.resolve().then(() => tc).then(({ showNotification: t }) => {
    t(e);
  });
}
function tl() {
  vn({
    type: Ve.INFORMATION,
    message: "The previous operation is still in progress. Please wait for it to finish."
  });
}
function nl(e) {
  return e.children && (e.children = e.children.filter(nl)), e.visible !== !1;
}
async function no() {
  return eo(() => {
    const e = window.Vaadin.devTools, t = e?.frontendConnection && e?.frontendConnection.status === "active";
    return e !== void 0 && t && e?.frontendConnection;
  });
}
function te(e, t) {
  no().then((n) => n.send(e, t));
}
const rl = () => {
  te("copilot-browser-info", {
    userAgent: navigator.userAgent,
    locale: navigator.language,
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
  });
}, _t = (e, t) => {
  te("copilot-track-event", { event: e, properties: t });
}, vu = (e, t) => {
  _t(e, { ...t, view: "react" });
}, pu = (e, t) => {
  _t(e, { ...t, view: "flow" });
};
class il {
  constructor() {
    this.spotlightActive = !1, this.welcomeActive = !1, this.loginCheckActive = !1, this.userInfo = void 0, this.active = !1, this.activatedFrom = null, this.activatedAtLeastOnce = !1, this.operationInProgress = void 0, this.operationWaitsHmrUpdate = void 0, this.operationWaitsHmrUpdateTimeout = void 0, this.idePluginState = void 0, this.notifications = [], this.infoTooltip = null, this.sectionPanelDragging = !1, this.spotlightDragging = !1, this.sectionPanelResizing = !1, this.drawerResizing = !1, this.jdkInfo = void 0, this.featureFlags = [], this.newVaadinVersionState = void 0, this.pointerEventsDisabledForScrolling = !1, this.editComponent = void 0, Xe(this, {
      notifications: S.shallow
    }), this.spotlightActive = fe.getSpotlightActivation() ?? !1;
  }
  setActive(t, n) {
    this.active = t, t && (this.activatedAtLeastOnce || (_t("activate"), this.idePluginState?.active && _t("plugin-active", {
      pluginVersion: this.idePluginState.version,
      ide: this.idePluginState.ide
    })), this.activatedAtLeastOnce = !0), this.activatedFrom = n ?? null;
  }
  setSpotlightActive(t) {
    this.spotlightActive = t;
  }
  setWelcomeActive(t) {
    this.welcomeActive = t;
  }
  setLoginCheckActive(t) {
    this.loginCheckActive = t;
  }
  setUserInfo(t) {
    this.userInfo = t;
  }
  startOperation(t) {
    if (this.operationInProgress)
      throw new Error(`An ${t} operation is already in progress`);
    if (this.operationWaitsHmrUpdate) {
      tl();
      return;
    }
    this.operationInProgress = t;
  }
  stopOperation(t) {
    if (this.operationInProgress) {
      if (this.operationInProgress !== t)
        return;
    } else return;
    this.operationInProgress = void 0;
  }
  setOperationWaitsHmrUpdate(t, n) {
    this.operationWaitsHmrUpdate = t, this.operationWaitsHmrUpdateTimeout = n;
  }
  clearOperationWaitsHmrUpdate() {
    this.operationWaitsHmrUpdate = void 0, this.operationWaitsHmrUpdateTimeout = void 0;
  }
  setIdePluginState(t) {
    this.idePluginState = t;
  }
  setJdkInfo(t) {
    this.jdkInfo = t;
  }
  toggleActive(t) {
    this.setActive(!this.active, this.active ? null : t ?? null);
  }
  reset() {
    this.active = !1, this.activatedAtLeastOnce = !1;
  }
  setNotifications(t) {
    this.notifications = t;
  }
  removeNotification(t) {
    t.animatingOut = !0, setTimeout(() => {
      this.reallyRemoveNotification(t);
    }, 180);
  }
  reallyRemoveNotification(t) {
    const n = this.notifications.indexOf(t);
    n > -1 && this.notifications.splice(n, 1);
  }
  setTooltip(t, n) {
    this.infoTooltip = {
      text: t,
      loader: n
    };
  }
  clearTooltip() {
    this.infoTooltip = null;
  }
  setSectionPanelDragging(t) {
    this.sectionPanelDragging = t;
  }
  setSpotlightDragging(t) {
    this.spotlightDragging = t;
  }
  setSectionPanelResizing(t) {
    this.sectionPanelResizing = t;
  }
  setDrawerResizing(t) {
    this.drawerResizing = t;
  }
  setFeatureFlags(t) {
    this.featureFlags = t;
  }
  setVaadinVersionState(t) {
    this.newVaadinVersionState = t;
  }
  setPointerEventsDisabledForScrolling(t) {
    this.pointerEventsDisabledForScrolling = t;
  }
  setEditComponent(t) {
    this.editComponent = t;
  }
  clearEditComponent() {
    this.editComponent = void 0;
  }
}
const gu = (e, t, n) => t >= e.left && t <= e.right && n >= e.top && n <= e.bottom, ol = (e) => {
  const t = [];
  let n = sl(e);
  for (; n; )
    t.push(n), n = n.parentElement;
  return t;
}, al = (e) => {
  if (e.length === 0)
    return new DOMRect();
  let t = Number.MAX_VALUE, n = Number.MAX_VALUE, r = Number.MIN_VALUE, i = Number.MIN_VALUE;
  const o = new DOMRect();
  return e.forEach((a) => {
    const s = a.getBoundingClientRect();
    s.x < t && (t = s.x), s.y < n && (n = s.y), s.right > r && (r = s.right), s.bottom > i && (i = s.bottom);
  }), o.x = t, o.y = n, o.width = r - t, o.height = i - n, o;
}, Ln = (e, t) => {
  let n = e;
  for (; !(n instanceof HTMLElement && n.localName === `${ve}main`); ) {
    if (!n.isConnected)
      return null;
    if (n.parentNode)
      n = n.parentNode;
    else if (n.host)
      n = n.host;
    else
      return null;
    if (n instanceof HTMLElement && n.localName === t)
      return n;
  }
  return null;
};
function sl(e) {
  return e.parentElement ?? e.parentNode?.host;
}
function zn(e) {
  if (e.assignedSlot)
    return zn(e.assignedSlot);
  if (e.parentElement)
    return e.parentElement;
  if (e.parentNode instanceof ShadowRoot)
    return e.parentNode.host;
}
function qe(e) {
  if (e instanceof Node) {
    const t = ol(e);
    return e instanceof HTMLElement && t.push(e), t.map((n) => n.localName).some((n) => n.startsWith(ve));
  }
  return !1;
}
function Nr(e) {
  return e instanceof Element;
}
function Cr(e) {
  return e.startsWith("vaadin-") ? e.substring(7).split("-").map((r) => r.charAt(0).toUpperCase() + r.slice(1)).join(" ") : e;
}
function Pr(e) {
  if (!e)
    return;
  if (e.id)
    return `#${e.id}`;
  if (!e.children)
    return;
  const t = Array.from(e.children).find((r) => r.localName === "label");
  if (t)
    return t.outerText.trim();
  const n = Array.from(e.childNodes).find(
    (r) => r.nodeType === Node.TEXT_NODE && r.textContent && r.textContent.trim().length > 0
  );
  if (n && n.textContent)
    return n.textContent.trim();
}
function ll() {
  let e = document.activeElement;
  for (; e?.shadowRoot && e.shadowRoot.activeElement; )
    e = e.shadowRoot.activeElement;
  return e;
}
function cl(e) {
  let t = zn(e);
  for (; t && t !== document.body; ) {
    const n = window.getComputedStyle(t), r = n.overflowY, i = n.overflowX, o = /(auto|scroll)/.test(r) && t.scrollHeight > t.clientHeight, a = /(auto|scroll)/.test(i) && t.scrollWidth > t.clientWidth;
    if (o || a)
      return t;
    t = zn(t);
  }
  return document.documentElement;
}
function ul(e, t) {
  return dl(e, t) && fl(t);
}
function dl(e, t) {
  const n = cl(e), r = n.getBoundingClientRect();
  if (n === document.documentElement || n === document.body) {
    const i = window.innerWidth || document.documentElement.clientWidth, o = window.innerHeight || document.documentElement.clientHeight;
    return t.top < o && t.bottom > 0 && t.left < i && t.right > 0;
  }
  return t.bottom > r.top && t.top < r.bottom && t.right > r.left && t.left < r.right;
}
function fl(e) {
  return e.bottom > 0 && e.right > 0 && e.top < window.innerHeight && e.left < window.innerWidth;
}
function mu(e) {
  return e instanceof HTMLElement;
}
function bu(e) {
  const t = ro(e), n = al(t);
  !t.every((i) => ul(i, n)) && t.length > 0 && t[0].scrollIntoView();
}
function ro(e) {
  const t = e;
  if (!t)
    return [];
  const { element: n } = t;
  if (n) {
    const r = t.element;
    if (n.localName === "vaadin-popover" || n.localName === "vaadin-dialog") {
      const i = r._overlayElement.shadowRoot.querySelector('[part="overlay"]');
      if (i)
        return [i];
    }
    return [n];
  }
  return t.children.flatMap((r) => ro(r));
}
var io = /* @__PURE__ */ ((e) => (e["vaadin-combo-box"] = "vaadin-combo-box", e["vaadin-date-picker"] = "vaadin-date-picker", e["vaadin-dialog"] = "vaadin-dialog", e["vaadin-multi-select-combo-box"] = "vaadin-multi-select-combo-box", e["vaadin-select"] = "vaadin-select", e["vaadin-time-picker"] = "vaadin-time-picker", e["vaadin-popover"] = "vaadin-popover", e))(io || {});
const nt = {
  "vaadin-combo-box": {
    hideOnActivation: !0,
    open: (e) => Tt(e),
    close: (e) => kt(e)
  },
  "vaadin-select": {
    hideOnActivation: !0,
    open: (e) => {
      const t = e;
      ao(t, t._overlayElement), t.opened = !0;
    },
    close: (e) => {
      const t = e;
      so(t, t._overlayElement), t.opened = !1;
    }
  },
  "vaadin-multi-select-combo-box": {
    hideOnActivation: !0,
    open: (e) => Tt(e.$.comboBox),
    close: (e) => {
      kt(e.$.comboBox), e.removeAttribute("focused");
    }
  },
  "vaadin-date-picker": {
    hideOnActivation: !0,
    open: (e) => Tt(e),
    close: (e) => kt(e)
  },
  "vaadin-time-picker": {
    hideOnActivation: !0,
    open: (e) => Tt(e.$.comboBox),
    close: (e) => {
      kt(e.$.comboBox), e.removeAttribute("focused");
    }
  },
  "vaadin-dialog": {
    hideOnActivation: !1
  },
  "vaadin-popover": {
    hideOnActivation: !1
  }
}, oo = (e) => {
  e.preventDefault(), e.stopImmediatePropagation();
}, Tt = (e) => {
  e.addEventListener("focusout", oo, { capture: !0 }), ao(e), e.opened = !0;
}, kt = (e) => {
  so(e), e.removeAttribute("focused"), e.removeEventListener("focusout", oo, { capture: !0 }), e.opened = !1;
}, ao = (e, t) => {
  const n = t ?? e.$.overlay;
  n.__oldModeless = n.modeless, n.modeless = !0;
}, so = (e, t) => {
  const n = t ?? e.$.overlay;
  n.modeless = n.__oldModeless !== void 0 ? n.__oldModeless : n.modeless, delete n.__oldModeless;
};
class hl {
  constructor() {
    this.openedOverlayOwners = /* @__PURE__ */ new Set(), this.overlayCloseEventListener = (t) => {
      qe(t.target?.owner) || (window.Vaadin.copilot._uiState.active || qe(t.detail.sourceEvent.target)) && (t.preventDefault(), t.stopImmediatePropagation());
    };
  }
  /**
   * Modifies pointer-events property to auto if dialog overlay is present on body element. <br/>
   * Overriding closeOnOutsideClick method in order to keep overlay present while copilot is active
   * @private
   */
  onCopilotActivation() {
    const t = Array.from(document.body.children).find(
      (r) => r.localName.startsWith("vaadin") && r.localName.endsWith("-overlay")
    );
    if (!t)
      return;
    const n = this.getOwner(t);
    if (n) {
      const r = nt[n.localName];
      if (!r)
        return;
      r.hideOnActivation && r.close ? r.close(n) : document.body.style.getPropertyValue("pointer-events") === "none" && document.body.style.removeProperty("pointer-events");
    }
  }
  /**
   * Restores pointer-events state on deactivation. <br/>
   * Closes opened overlays while using copilot.
   * @private
   */
  onCopilotDeactivation() {
    this.openedOverlayOwners.forEach((n) => {
      const r = nt[n.localName];
      r && r.close && r.close(n);
    }), document.body.querySelector("vaadin-dialog-overlay") && document.body.style.setProperty("pointer-events", "none");
  }
  getOwner(t) {
    const n = t;
    return n.owner ?? n.__dataHost;
  }
  addOverlayOutsideClickEvent() {
    document.documentElement.addEventListener("vaadin-overlay-outside-click", this.overlayCloseEventListener, {
      capture: !0
    }), document.documentElement.addEventListener("vaadin-overlay-escape-press", this.overlayCloseEventListener, {
      capture: !0
    });
  }
  removeOverlayOutsideClickEvent() {
    document.documentElement.removeEventListener("vaadin-overlay-outside-click", this.overlayCloseEventListener), document.documentElement.removeEventListener("vaadin-overlay-escape-press", this.overlayCloseEventListener);
  }
  toggle(t) {
    const n = nt[t.localName];
    this.isOverlayActive(t) ? (n.close(t), this.openedOverlayOwners.delete(t)) : (n.open(t), this.openedOverlayOwners.add(t));
  }
  isOverlayActive(t) {
    const n = nt[t.localName];
    return n.active ? n.active(t) : t.hasAttribute("opened");
  }
  overlayStatus(t) {
    if (!t)
      return { visible: !1 };
    const n = t.localName;
    let r = Object.keys(io).includes(n);
    if (!r)
      return { visible: !1 };
    const i = nt[t.localName];
    i.hasOverlay && (r = i.hasOverlay(t));
    const o = this.isOverlayActive(t);
    return { visible: r, active: o };
  }
}
class lo {
  constructor() {
    this.promise = new Promise((t) => {
      this.resolveInit = t;
    });
  }
  done(t) {
    this.resolveInit(t);
  }
}
class vl {
  constructor() {
    this.dismissedNotifications = [], this.termsSummaryDismissed = !1, this.activationButtonPosition = null, this.paletteState = null, this.activationShortcut = !0, this.activationAnimation = !0, this.recentSwitchedUsernames = [], this.newVersionPreReleasesVisible = !1, this.aiUsageAllowed = "ask", this.sendErrorReportsAllowed = !0, Xe(this), this.initializer = new lo(), this.initializer.promise.then(() => {
      Zn(
        () => JSON.stringify(this),
        () => {
          te("copilot-set-machine-configuration", { conf: JSON.stringify($r(this)) });
        }
      );
    }), window.Vaadin.copilot.eventbus.on("copilot-machine-configuration", (t) => {
      const n = t.detail.conf;
      Object.assign(this, $r(n)), this.initializer.done(!0), t.preventDefault();
    }), this.loadData();
  }
  loadData() {
    te("copilot-get-machine-configuration", {});
  }
  addDismissedNotification(t) {
    this.dismissedNotifications = [...this.dismissedNotifications, t];
  }
  getDismissedNotifications() {
    return this.dismissedNotifications;
  }
  clearDismissedNotifications() {
    this.dismissedNotifications = [];
  }
  setTermsSummaryDismissed(t) {
    this.termsSummaryDismissed = t;
  }
  isTermsSummaryDismissed() {
    return this.termsSummaryDismissed;
  }
  getActivationButtonPosition() {
    return this.activationButtonPosition;
  }
  setActivationButtonPosition(t) {
    this.activationButtonPosition = t;
  }
  getPaletteState() {
    return this.paletteState;
  }
  setPaletteState(t) {
    this.paletteState = t;
  }
  isActivationShortcut() {
    return this.activationShortcut;
  }
  setActivationShortcut(t) {
    this.activationShortcut = t;
  }
  isActivationAnimation() {
    return this.activationAnimation;
  }
  setActivationAnimation(t) {
    this.activationAnimation = t;
  }
  getRecentSwitchedUsernames() {
    return this.recentSwitchedUsernames;
  }
  setRecentSwitchedUsernames(t) {
    this.recentSwitchedUsernames = t;
  }
  getNewVersionPreReleasesVisible() {
    return this.newVersionPreReleasesVisible;
  }
  setNewVersionPreReleasesVisible(t) {
    this.newVersionPreReleasesVisible = t;
  }
  setSendErrorReportsAllowed(t) {
    this.sendErrorReportsAllowed = t;
  }
  isSendErrorReportsAllowed() {
    return this.sendErrorReportsAllowed;
  }
  setAIUsageAllowed(t) {
    this.aiUsageAllowed = t;
  }
  isAIUsageAllowed() {
    return this.aiUsageAllowed;
  }
}
function $r(e) {
  const t = { ...e };
  return delete t.initializer, t;
}
class pl {
  constructor() {
    this._previewActivated = !1, this._remainingTimeInMillis = -1, this._active = !1, this._configurationLoaded = !1, Xe(this);
  }
  setConfiguration(t) {
    this._previewActivated = t.previewActivated, t.previewActivated ? this._remainingTimeInMillis = t.remainingTimeInMillis : this._remainingTimeInMillis = -1, this._active = t.active, this._configurationLoaded = !0;
  }
  get previewActivated() {
    return this._previewActivated;
  }
  get remainingTimeInMillis() {
    return this._remainingTimeInMillis;
  }
  get active() {
    return this._active;
  }
  get configurationLoaded() {
    return this._configurationLoaded;
  }
  get expired() {
    return this.previewActivated && !this.active;
  }
  reset() {
    this._previewActivated = !1, this._active = !1, this._configurationLoaded = !1, this._remainingTimeInMillis = -1;
  }
  loadPreviewConfiguration() {
    hn(`${ve}get-preview`, {}, (t) => {
      const n = t.data;
      this.setConfiguration(n);
    }).catch((t) => {
      Promise.resolve().then(() => fc).then((n) => {
        n.handleCopilotError("Load preview configuration failed", t);
      });
    });
  }
}
class gl {
  constructor() {
    this._panels = [], this._attentionRequiredPanelTag = null, this._floatingPanelsZIndexOrder = [], this.renderedPanels = /* @__PURE__ */ new Set(), this.customTags = /* @__PURE__ */ new Map(), Xe(this), this.restorePositions();
  }
  shouldRender(t) {
    return this.renderedPanels.has(t);
  }
  restorePositions() {
    const t = fe.getPanelConfigurations();
    t && (this._panels = this._panels.map((n) => {
      const r = t.find((i) => i.tag === n.tag);
      return r && (n = Object.assign(n, { ...r })), n;
    }));
  }
  /**
   * Brings a given floating panel to the front.
   *
   * @param panelTag the tag name of the panel
   */
  bringToFront(t) {
    this._floatingPanelsZIndexOrder = this._floatingPanelsZIndexOrder.filter((n) => n !== t), this.getPanelByTag(t)?.floating && this._floatingPanelsZIndexOrder.push(t);
  }
  /**
   * Returns the focused z-index of floating panel as following order
   * <ul>
   *     <li>Returns 50 for last(focused) element </li>
   *     <li>Returns the index of element in list(starting from 0) </li>
   *     <li>Returns 0 if panel is not in the list</li>
   * </ul>
   * @param panelTag
   */
  getFloatingPanelZIndex(t) {
    const n = this._floatingPanelsZIndexOrder.findIndex((r) => r === t);
    return n === this._floatingPanelsZIndexOrder.length - 1 ? 50 : n === -1 ? 0 : n;
  }
  get floatingPanelsZIndexOrder() {
    return this._floatingPanelsZIndexOrder;
  }
  get attentionRequiredPanelTag() {
    return this._attentionRequiredPanelTag;
  }
  set attentionRequiredPanelTag(t) {
    this._attentionRequiredPanelTag = t;
  }
  getAttentionRequiredPanelConfiguration() {
    return this._panels.find((t) => t.tag === this._attentionRequiredPanelTag);
  }
  clearAttention() {
    this._attentionRequiredPanelTag = null;
  }
  get panels() {
    return this._panels;
  }
  addPanel(t) {
    if (this.getPanelByTag(t.tag))
      return;
    this._panels.push(t), this.restorePositions();
    const n = this.getPanelByTag(t.tag);
    if (n)
      (n.eager || n.expanded) && this.renderedPanels.add(t.tag);
    else throw new Error(`Panel configuration not found for tag ${t.tag}`);
  }
  getPanelByTag(t) {
    return this._panels.find((n) => n.tag === t);
  }
  updatePanel(t, n) {
    const r = [...this._panels], i = r.find((o) => o.tag === t);
    if (i) {
      for (const o in n)
        i[o] = n[o];
      i.expanded && this.renderedPanels.add(i.tag), n.floating === !1 && (this._floatingPanelsZIndexOrder = this._floatingPanelsZIndexOrder.filter((o) => o !== t)), this._panels = r, fe.savePanelConfigurations(this._panels);
    }
  }
  updateOrders(t) {
    const n = [...this._panels];
    n.forEach((r) => {
      const i = t.find((o) => o.tag === r.tag);
      i && (r.panelOrder = i.order);
    }), this._panels = n, fe.savePanelConfigurations(n);
  }
  removePanel(t) {
    const n = this._panels.findIndex((r) => r.tag === t);
    n < 0 || (this._panels.splice(n, 1), fe.savePanelConfigurations(this._panels));
  }
  setCustomPanelHeader(t, n) {
    this.customTags.set(t.tag, n);
  }
  getPanelHeader(t) {
    return this.customTags.get(t.tag) ?? t.header;
  }
  clearCustomPanelHeader(t) {
    this.customTags.delete(t.tag);
  }
}
class ml {
  constructor() {
    this.supportsHilla = !0, this.springSecurityEnabled = !1, this.springJpaDataEnabled = !1, this.urlPrefix = "", Xe(this);
  }
  setSupportsHilla(t) {
    this.supportsHilla = t;
  }
  setSpringSecurityEnabled(t) {
    this.springSecurityEnabled = t;
  }
  setSpringJpaDataEnabled(t) {
    this.springJpaDataEnabled = t;
  }
  setUrlPrefix(t) {
    this.urlPrefix = t;
  }
}
class bl {
  constructor() {
    this.palette = { components: [] }, Xe(this), this.initializer = new lo(), this.initializer.promise.then(() => {
      Zn(
        () => JSON.stringify(this),
        () => {
          te("copilot-set-project-state-configuration", { conf: JSON.stringify(Dr(this)) });
        }
      );
    }), window.Vaadin.copilot.eventbus.on("copilot-project-state-configuration", (t) => {
      const n = t.detail.conf;
      Object.assign(this, Dr(n)), this.initializer.done(!0), t.preventDefault();
    }), this.loadData();
  }
  loadData() {
    te("copilot-get-project-state-configuration", {});
  }
  addPaletteCustomComponent(t) {
    return (this.palette?.components ?? []).find((i) => On(i, t)) ? !1 : (this.palette || (this.palette = { components: [] }), this.palette = JSON.parse(JSON.stringify(this.palette)), this.palette.components.push(t), !0);
  }
  removePaletteCustomComponent(t) {
    if (this.palette) {
      const n = this.palette.components.findIndex(
        (r) => On(r, t)
      );
      n > -1 && this.palette.components.splice(n, 1);
    }
  }
  updatePaletteCustomComponent(t, n) {
    if (!this.palette || !this.palette.components)
      return;
    const r = [...this.palette.components], i = r.findIndex((o) => On(o, t));
    i !== -1 && (r[i] = { ...t, ...n }), this.palette.components = r;
  }
  paletteCustomComponentExist(t, n) {
    return !this.palette || !this.palette.components ? !1 : t ? this.palette.components.findIndex(
      (r) => r.java && !r.react && r.javaClassName === t
    ) !== -1 : n ? this.palette.components.findIndex((r) => !r.java && r.react && r.template === n) !== -1 : !1;
  }
  get paletteComponents() {
    return this.palette?.components || [];
  }
}
function Dr(e) {
  const t = { ...e };
  return delete t.initializer, t;
}
function On(e, t) {
  return e.java ? t.java ? e.javaClassName === t.javaClassName : !1 : e.react && t.react ? e.template === t.template : !1;
}
window.Vaadin ??= {};
window.Vaadin.copilot ??= {};
window.Vaadin.copilot.plugins = [];
window.Vaadin.copilot._uiState = new il();
window.Vaadin.copilot.eventbus = new Vo();
window.Vaadin.copilot.overlayManager = new hl();
window.Vaadin.copilot._machineState = new vl();
window.Vaadin.copilot._storedProjectState = new bl();
window.Vaadin.copilot._previewState = new pl();
window.Vaadin.copilot._sectionPanelUiState = new gl();
window.Vaadin.copilot._earlyProjectState = new ml();
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const _l = (e) => (t, n) => {
  n !== void 0 ? n.addInitializer(() => {
    customElements.define(e, t);
  }) : customElements.define(e, t);
};
/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const Mt = globalThis, nr = Mt.ShadowRoot && (Mt.ShadyCSS === void 0 || Mt.ShadyCSS.nativeShadow) && "adoptedStyleSheets" in Document.prototype && "replace" in CSSStyleSheet.prototype, rr = Symbol(), Tr = /* @__PURE__ */ new WeakMap();
let co = class {
  constructor(t, n, r) {
    if (this._$cssResult$ = !0, r !== rr) throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");
    this.cssText = t, this.t = n;
  }
  get styleSheet() {
    let t = this.o;
    const n = this.t;
    if (nr && t === void 0) {
      const r = n !== void 0 && n.length === 1;
      r && (t = Tr.get(n)), t === void 0 && ((this.o = t = new CSSStyleSheet()).replaceSync(this.cssText), r && Tr.set(n, t));
    }
    return t;
  }
  toString() {
    return this.cssText;
  }
};
const J = (e) => new co(typeof e == "string" ? e : e + "", void 0, rr), yl = (e, ...t) => {
  const n = e.length === 1 ? e[0] : t.reduce((r, i, o) => r + ((a) => {
    if (a._$cssResult$ === !0) return a.cssText;
    if (typeof a == "number") return a;
    throw Error("Value passed to 'css' function must be a 'css' function result: " + a + ". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.");
  })(i) + e[o + 1], e[0]);
  return new co(n, e, rr);
}, wl = (e, t) => {
  if (nr) e.adoptedStyleSheets = t.map((n) => n instanceof CSSStyleSheet ? n : n.styleSheet);
  else for (const n of t) {
    const r = document.createElement("style"), i = Mt.litNonce;
    i !== void 0 && r.setAttribute("nonce", i), r.textContent = n.cssText, e.appendChild(r);
  }
}, kr = nr ? (e) => e : (e) => e instanceof CSSStyleSheet ? ((t) => {
  let n = "";
  for (const r of t.cssRules) n += r.cssText;
  return J(n);
})(e) : e;
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const { is: El, defineProperty: Ol, getOwnPropertyDescriptor: Al, getOwnPropertyNames: Sl, getOwnPropertySymbols: xl, getPrototypeOf: Nl } = Object, pn = globalThis, Ir = pn.trustedTypes, Cl = Ir ? Ir.emptyScript : "", Pl = pn.reactiveElementPolyfillSupport, ft = (e, t) => e, Un = { toAttribute(e, t) {
  switch (t) {
    case Boolean:
      e = e ? Cl : null;
      break;
    case Object:
    case Array:
      e = e == null ? e : JSON.stringify(e);
  }
  return e;
}, fromAttribute(e, t) {
  let n = e;
  switch (t) {
    case Boolean:
      n = e !== null;
      break;
    case Number:
      n = e === null ? null : Number(e);
      break;
    case Object:
    case Array:
      try {
        n = JSON.parse(e);
      } catch {
        n = null;
      }
  }
  return n;
} }, uo = (e, t) => !El(e, t), Vr = { attribute: !0, type: String, converter: Un, reflect: !1, hasChanged: uo };
Symbol.metadata ??= Symbol("metadata"), pn.litPropertyMetadata ??= /* @__PURE__ */ new WeakMap();
let Me = class extends HTMLElement {
  static addInitializer(t) {
    this._$Ei(), (this.l ??= []).push(t);
  }
  static get observedAttributes() {
    return this.finalize(), this._$Eh && [...this._$Eh.keys()];
  }
  static createProperty(t, n = Vr) {
    if (n.state && (n.attribute = !1), this._$Ei(), this.elementProperties.set(t, n), !n.noAccessor) {
      const r = Symbol(), i = this.getPropertyDescriptor(t, r, n);
      i !== void 0 && Ol(this.prototype, t, i);
    }
  }
  static getPropertyDescriptor(t, n, r) {
    const { get: i, set: o } = Al(this.prototype, t) ?? { get() {
      return this[n];
    }, set(a) {
      this[n] = a;
    } };
    return { get() {
      return i?.call(this);
    }, set(a) {
      const s = i?.call(this);
      o.call(this, a), this.requestUpdate(t, s, r);
    }, configurable: !0, enumerable: !0 };
  }
  static getPropertyOptions(t) {
    return this.elementProperties.get(t) ?? Vr;
  }
  static _$Ei() {
    if (this.hasOwnProperty(ft("elementProperties"))) return;
    const t = Nl(this);
    t.finalize(), t.l !== void 0 && (this.l = [...t.l]), this.elementProperties = new Map(t.elementProperties);
  }
  static finalize() {
    if (this.hasOwnProperty(ft("finalized"))) return;
    if (this.finalized = !0, this._$Ei(), this.hasOwnProperty(ft("properties"))) {
      const n = this.properties, r = [...Sl(n), ...xl(n)];
      for (const i of r) this.createProperty(i, n[i]);
    }
    const t = this[Symbol.metadata];
    if (t !== null) {
      const n = litPropertyMetadata.get(t);
      if (n !== void 0) for (const [r, i] of n) this.elementProperties.set(r, i);
    }
    this._$Eh = /* @__PURE__ */ new Map();
    for (const [n, r] of this.elementProperties) {
      const i = this._$Eu(n, r);
      i !== void 0 && this._$Eh.set(i, n);
    }
    this.elementStyles = this.finalizeStyles(this.styles);
  }
  static finalizeStyles(t) {
    const n = [];
    if (Array.isArray(t)) {
      const r = new Set(t.flat(1 / 0).reverse());
      for (const i of r) n.unshift(kr(i));
    } else t !== void 0 && n.push(kr(t));
    return n;
  }
  static _$Eu(t, n) {
    const r = n.attribute;
    return r === !1 ? void 0 : typeof r == "string" ? r : typeof t == "string" ? t.toLowerCase() : void 0;
  }
  constructor() {
    super(), this._$Ep = void 0, this.isUpdatePending = !1, this.hasUpdated = !1, this._$Em = null, this._$Ev();
  }
  _$Ev() {
    this._$ES = new Promise((t) => this.enableUpdating = t), this._$AL = /* @__PURE__ */ new Map(), this._$E_(), this.requestUpdate(), this.constructor.l?.forEach((t) => t(this));
  }
  addController(t) {
    (this._$EO ??= /* @__PURE__ */ new Set()).add(t), this.renderRoot !== void 0 && this.isConnected && t.hostConnected?.();
  }
  removeController(t) {
    this._$EO?.delete(t);
  }
  _$E_() {
    const t = /* @__PURE__ */ new Map(), n = this.constructor.elementProperties;
    for (const r of n.keys()) this.hasOwnProperty(r) && (t.set(r, this[r]), delete this[r]);
    t.size > 0 && (this._$Ep = t);
  }
  createRenderRoot() {
    const t = this.shadowRoot ?? this.attachShadow(this.constructor.shadowRootOptions);
    return wl(t, this.constructor.elementStyles), t;
  }
  connectedCallback() {
    this.renderRoot ??= this.createRenderRoot(), this.enableUpdating(!0), this._$EO?.forEach((t) => t.hostConnected?.());
  }
  enableUpdating(t) {
  }
  disconnectedCallback() {
    this._$EO?.forEach((t) => t.hostDisconnected?.());
  }
  attributeChangedCallback(t, n, r) {
    this._$AK(t, r);
  }
  _$EC(t, n) {
    const r = this.constructor.elementProperties.get(t), i = this.constructor._$Eu(t, r);
    if (i !== void 0 && r.reflect === !0) {
      const o = (r.converter?.toAttribute !== void 0 ? r.converter : Un).toAttribute(n, r.type);
      this._$Em = t, o == null ? this.removeAttribute(i) : this.setAttribute(i, o), this._$Em = null;
    }
  }
  _$AK(t, n) {
    const r = this.constructor, i = r._$Eh.get(t);
    if (i !== void 0 && this._$Em !== i) {
      const o = r.getPropertyOptions(i), a = typeof o.converter == "function" ? { fromAttribute: o.converter } : o.converter?.fromAttribute !== void 0 ? o.converter : Un;
      this._$Em = i, this[i] = a.fromAttribute(n, o.type), this._$Em = null;
    }
  }
  requestUpdate(t, n, r) {
    if (t !== void 0) {
      if (r ??= this.constructor.getPropertyOptions(t), !(r.hasChanged ?? uo)(this[t], n)) return;
      this.P(t, n, r);
    }
    this.isUpdatePending === !1 && (this._$ES = this._$ET());
  }
  P(t, n, r) {
    this._$AL.has(t) || this._$AL.set(t, n), r.reflect === !0 && this._$Em !== t && (this._$Ej ??= /* @__PURE__ */ new Set()).add(t);
  }
  async _$ET() {
    this.isUpdatePending = !0;
    try {
      await this._$ES;
    } catch (n) {
      Promise.reject(n);
    }
    const t = this.scheduleUpdate();
    return t != null && await t, !this.isUpdatePending;
  }
  scheduleUpdate() {
    return this.performUpdate();
  }
  performUpdate() {
    if (!this.isUpdatePending) return;
    if (!this.hasUpdated) {
      if (this.renderRoot ??= this.createRenderRoot(), this._$Ep) {
        for (const [i, o] of this._$Ep) this[i] = o;
        this._$Ep = void 0;
      }
      const r = this.constructor.elementProperties;
      if (r.size > 0) for (const [i, o] of r) o.wrapped !== !0 || this._$AL.has(i) || this[i] === void 0 || this.P(i, this[i], o);
    }
    let t = !1;
    const n = this._$AL;
    try {
      t = this.shouldUpdate(n), t ? (this.willUpdate(n), this._$EO?.forEach((r) => r.hostUpdate?.()), this.update(n)) : this._$EU();
    } catch (r) {
      throw t = !1, this._$EU(), r;
    }
    t && this._$AE(n);
  }
  willUpdate(t) {
  }
  _$AE(t) {
    this._$EO?.forEach((n) => n.hostUpdated?.()), this.hasUpdated || (this.hasUpdated = !0, this.firstUpdated(t)), this.updated(t);
  }
  _$EU() {
    this._$AL = /* @__PURE__ */ new Map(), this.isUpdatePending = !1;
  }
  get updateComplete() {
    return this.getUpdateComplete();
  }
  getUpdateComplete() {
    return this._$ES;
  }
  shouldUpdate(t) {
    return !0;
  }
  update(t) {
    this._$Ej &&= this._$Ej.forEach((n) => this._$EC(n, this[n])), this._$EU();
  }
  updated(t) {
  }
  firstUpdated(t) {
  }
};
Me.elementStyles = [], Me.shadowRootOptions = { mode: "open" }, Me[ft("elementProperties")] = /* @__PURE__ */ new Map(), Me[ft("finalized")] = /* @__PURE__ */ new Map(), Pl?.({ ReactiveElement: Me }), (pn.reactiveElementVersions ??= []).push("2.0.4");
const je = Symbol("LitMobxRenderReaction"), Rr = Symbol("LitMobxRequestUpdate");
function $l(e, t) {
  var n, r;
  return r = class extends e {
    constructor() {
      super(...arguments), this[n] = () => {
        this.requestUpdate();
      };
    }
    connectedCallback() {
      super.connectedCallback();
      const o = this.constructor.name || this.nodeName;
      this[je] = new t(`${o}.update()`, this[Rr]), this.hasUpdated && this.requestUpdate();
    }
    disconnectedCallback() {
      super.disconnectedCallback(), this[je] && (this[je].dispose(), this[je] = void 0);
    }
    update(o) {
      this[je] ? this[je].track(super.update.bind(this, o)) : super.update(o);
    }
  }, n = Rr, r;
}
function Dl(e) {
  return $l(e, ee);
}
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const ir = globalThis, en = ir.trustedTypes, jr = en ? en.createPolicy("lit-html", { createHTML: (e) => e }) : void 0, fo = "$lit$", ue = `lit$${Math.random().toFixed(9).slice(2)}$`, ho = "?" + ue, Tl = `<${ho}>`, De = document, yt = () => De.createComment(""), wt = (e) => e === null || typeof e != "object" && typeof e != "function", or = Array.isArray, kl = (e) => or(e) || typeof e?.[Symbol.iterator] == "function", An = `[ 	
\f\r]`, rt = /<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g, Mr = /-->/g, Lr = />/g, ye = RegExp(`>|${An}(?:([^\\s"'>=/]+)(${An}*=${An}*(?:[^ 	
\f\r"'\`<>=]|("|')|))|$)`, "g"), zr = /'/g, Ur = /"/g, vo = /^(?:script|style|textarea|title)$/i, po = (e) => (t, ...n) => ({ _$litType$: e, strings: t, values: n }), Ue = po(1), Eu = po(2), pe = Symbol.for("lit-noChange"), O = Symbol.for("lit-nothing"), Br = /* @__PURE__ */ new WeakMap(), Ae = De.createTreeWalker(De, 129);
function go(e, t) {
  if (!or(e) || !e.hasOwnProperty("raw")) throw Error("invalid template strings array");
  return jr !== void 0 ? jr.createHTML(t) : t;
}
const Il = (e, t) => {
  const n = e.length - 1, r = [];
  let i, o = t === 2 ? "<svg>" : t === 3 ? "<math>" : "", a = rt;
  for (let s = 0; s < n; s++) {
    const l = e[s];
    let c, u, d = -1, v = 0;
    for (; v < l.length && (a.lastIndex = v, u = a.exec(l), u !== null); ) v = a.lastIndex, a === rt ? u[1] === "!--" ? a = Mr : u[1] !== void 0 ? a = Lr : u[2] !== void 0 ? (vo.test(u[2]) && (i = RegExp("</" + u[2], "g")), a = ye) : u[3] !== void 0 && (a = ye) : a === ye ? u[0] === ">" ? (a = i ?? rt, d = -1) : u[1] === void 0 ? d = -2 : (d = a.lastIndex - u[2].length, c = u[1], a = u[3] === void 0 ? ye : u[3] === '"' ? Ur : zr) : a === Ur || a === zr ? a = ye : a === Mr || a === Lr ? a = rt : (a = ye, i = void 0);
    const p = a === ye && e[s + 1].startsWith("/>") ? " " : "";
    o += a === rt ? l + Tl : d >= 0 ? (r.push(c), l.slice(0, d) + fo + l.slice(d) + ue + p) : l + ue + (d === -2 ? s : p);
  }
  return [go(e, o + (e[n] || "<?>") + (t === 2 ? "</svg>" : t === 3 ? "</math>" : "")), r];
};
class Et {
  constructor({ strings: t, _$litType$: n }, r) {
    let i;
    this.parts = [];
    let o = 0, a = 0;
    const s = t.length - 1, l = this.parts, [c, u] = Il(t, n);
    if (this.el = Et.createElement(c, r), Ae.currentNode = this.el.content, n === 2 || n === 3) {
      const d = this.el.content.firstChild;
      d.replaceWith(...d.childNodes);
    }
    for (; (i = Ae.nextNode()) !== null && l.length < s; ) {
      if (i.nodeType === 1) {
        if (i.hasAttributes()) for (const d of i.getAttributeNames()) if (d.endsWith(fo)) {
          const v = u[a++], p = i.getAttribute(d).split(ue), b = /([.?@])?(.*)/.exec(v);
          l.push({ type: 1, index: o, name: b[2], strings: p, ctor: b[1] === "." ? Rl : b[1] === "?" ? jl : b[1] === "@" ? Ml : gn }), i.removeAttribute(d);
        } else d.startsWith(ue) && (l.push({ type: 6, index: o }), i.removeAttribute(d));
        if (vo.test(i.tagName)) {
          const d = i.textContent.split(ue), v = d.length - 1;
          if (v > 0) {
            i.textContent = en ? en.emptyScript : "";
            for (let p = 0; p < v; p++) i.append(d[p], yt()), Ae.nextNode(), l.push({ type: 2, index: ++o });
            i.append(d[v], yt());
          }
        }
      } else if (i.nodeType === 8) if (i.data === ho) l.push({ type: 2, index: o });
      else {
        let d = -1;
        for (; (d = i.data.indexOf(ue, d + 1)) !== -1; ) l.push({ type: 7, index: o }), d += ue.length - 1;
      }
      o++;
    }
  }
  static createElement(t, n) {
    const r = De.createElement("template");
    return r.innerHTML = t, r;
  }
}
function We(e, t, n = e, r) {
  if (t === pe) return t;
  let i = r !== void 0 ? n._$Co?.[r] : n._$Cl;
  const o = wt(t) ? void 0 : t._$litDirective$;
  return i?.constructor !== o && (i?._$AO?.(!1), o === void 0 ? i = void 0 : (i = new o(e), i._$AT(e, n, r)), r !== void 0 ? (n._$Co ??= [])[r] = i : n._$Cl = i), i !== void 0 && (t = We(e, i._$AS(e, t.values), i, r)), t;
}
let Vl = class {
  constructor(t, n) {
    this._$AV = [], this._$AN = void 0, this._$AD = t, this._$AM = n;
  }
  get parentNode() {
    return this._$AM.parentNode;
  }
  get _$AU() {
    return this._$AM._$AU;
  }
  u(t) {
    const { el: { content: n }, parts: r } = this._$AD, i = (t?.creationScope ?? De).importNode(n, !0);
    Ae.currentNode = i;
    let o = Ae.nextNode(), a = 0, s = 0, l = r[0];
    for (; l !== void 0; ) {
      if (a === l.index) {
        let c;
        l.type === 2 ? c = new Qe(o, o.nextSibling, this, t) : l.type === 1 ? c = new l.ctor(o, l.name, l.strings, this, t) : l.type === 6 && (c = new Ll(o, this, t)), this._$AV.push(c), l = r[++s];
      }
      a !== l?.index && (o = Ae.nextNode(), a++);
    }
    return Ae.currentNode = De, i;
  }
  p(t) {
    let n = 0;
    for (const r of this._$AV) r !== void 0 && (r.strings !== void 0 ? (r._$AI(t, r, n), n += r.strings.length - 2) : r._$AI(t[n])), n++;
  }
};
class Qe {
  get _$AU() {
    return this._$AM?._$AU ?? this._$Cv;
  }
  constructor(t, n, r, i) {
    this.type = 2, this._$AH = O, this._$AN = void 0, this._$AA = t, this._$AB = n, this._$AM = r, this.options = i, this._$Cv = i?.isConnected ?? !0;
  }
  get parentNode() {
    let t = this._$AA.parentNode;
    const n = this._$AM;
    return n !== void 0 && t?.nodeType === 11 && (t = n.parentNode), t;
  }
  get startNode() {
    return this._$AA;
  }
  get endNode() {
    return this._$AB;
  }
  _$AI(t, n = this) {
    t = We(this, t, n), wt(t) ? t === O || t == null || t === "" ? (this._$AH !== O && this._$AR(), this._$AH = O) : t !== this._$AH && t !== pe && this._(t) : t._$litType$ !== void 0 ? this.$(t) : t.nodeType !== void 0 ? this.T(t) : kl(t) ? this.k(t) : this._(t);
  }
  O(t) {
    return this._$AA.parentNode.insertBefore(t, this._$AB);
  }
  T(t) {
    this._$AH !== t && (this._$AR(), this._$AH = this.O(t));
  }
  _(t) {
    this._$AH !== O && wt(this._$AH) ? this._$AA.nextSibling.data = t : this.T(De.createTextNode(t)), this._$AH = t;
  }
  $(t) {
    const { values: n, _$litType$: r } = t, i = typeof r == "number" ? this._$AC(t) : (r.el === void 0 && (r.el = Et.createElement(go(r.h, r.h[0]), this.options)), r);
    if (this._$AH?._$AD === i) this._$AH.p(n);
    else {
      const o = new Vl(i, this), a = o.u(this.options);
      o.p(n), this.T(a), this._$AH = o;
    }
  }
  _$AC(t) {
    let n = Br.get(t.strings);
    return n === void 0 && Br.set(t.strings, n = new Et(t)), n;
  }
  k(t) {
    or(this._$AH) || (this._$AH = [], this._$AR());
    const n = this._$AH;
    let r, i = 0;
    for (const o of t) i === n.length ? n.push(r = new Qe(this.O(yt()), this.O(yt()), this, this.options)) : r = n[i], r._$AI(o), i++;
    i < n.length && (this._$AR(r && r._$AB.nextSibling, i), n.length = i);
  }
  _$AR(t = this._$AA.nextSibling, n) {
    for (this._$AP?.(!1, !0, n); t && t !== this._$AB; ) {
      const r = t.nextSibling;
      t.remove(), t = r;
    }
  }
  setConnected(t) {
    this._$AM === void 0 && (this._$Cv = t, this._$AP?.(t));
  }
}
class gn {
  get tagName() {
    return this.element.tagName;
  }
  get _$AU() {
    return this._$AM._$AU;
  }
  constructor(t, n, r, i, o) {
    this.type = 1, this._$AH = O, this._$AN = void 0, this.element = t, this.name = n, this._$AM = i, this.options = o, r.length > 2 || r[0] !== "" || r[1] !== "" ? (this._$AH = Array(r.length - 1).fill(new String()), this.strings = r) : this._$AH = O;
  }
  _$AI(t, n = this, r, i) {
    const o = this.strings;
    let a = !1;
    if (o === void 0) t = We(this, t, n, 0), a = !wt(t) || t !== this._$AH && t !== pe, a && (this._$AH = t);
    else {
      const s = t;
      let l, c;
      for (t = o[0], l = 0; l < o.length - 1; l++) c = We(this, s[r + l], n, l), c === pe && (c = this._$AH[l]), a ||= !wt(c) || c !== this._$AH[l], c === O ? t = O : t !== O && (t += (c ?? "") + o[l + 1]), this._$AH[l] = c;
    }
    a && !i && this.j(t);
  }
  j(t) {
    t === O ? this.element.removeAttribute(this.name) : this.element.setAttribute(this.name, t ?? "");
  }
}
class Rl extends gn {
  constructor() {
    super(...arguments), this.type = 3;
  }
  j(t) {
    this.element[this.name] = t === O ? void 0 : t;
  }
}
class jl extends gn {
  constructor() {
    super(...arguments), this.type = 4;
  }
  j(t) {
    this.element.toggleAttribute(this.name, !!t && t !== O);
  }
}
class Ml extends gn {
  constructor(t, n, r, i, o) {
    super(t, n, r, i, o), this.type = 5;
  }
  _$AI(t, n = this) {
    if ((t = We(this, t, n, 0) ?? O) === pe) return;
    const r = this._$AH, i = t === O && r !== O || t.capture !== r.capture || t.once !== r.once || t.passive !== r.passive, o = t !== O && (r === O || i);
    i && this.element.removeEventListener(this.name, this, r), o && this.element.addEventListener(this.name, this, t), this._$AH = t;
  }
  handleEvent(t) {
    typeof this._$AH == "function" ? this._$AH.call(this.options?.host ?? this.element, t) : this._$AH.handleEvent(t);
  }
}
class Ll {
  constructor(t, n, r) {
    this.element = t, this.type = 6, this._$AN = void 0, this._$AM = n, this.options = r;
  }
  get _$AU() {
    return this._$AM._$AU;
  }
  _$AI(t) {
    We(this, t);
  }
}
const zl = { I: Qe }, Ul = ir.litHtmlPolyfillSupport;
Ul?.(Et, Qe), (ir.litHtmlVersions ??= []).push("3.2.1");
const Bl = (e, t, n) => {
  const r = n?.renderBefore ?? t;
  let i = r._$litPart$;
  if (i === void 0) {
    const o = n?.renderBefore ?? null;
    r._$litPart$ = i = new Qe(t.insertBefore(yt(), o), o, void 0, n ?? {});
  }
  return i._$AI(e), i;
};
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
let ht = class extends Me {
  constructor() {
    super(...arguments), this.renderOptions = { host: this }, this._$Do = void 0;
  }
  createRenderRoot() {
    const t = super.createRenderRoot();
    return this.renderOptions.renderBefore ??= t.firstChild, t;
  }
  update(t) {
    const n = this.render();
    this.hasUpdated || (this.renderOptions.isConnected = this.isConnected), super.update(t), this._$Do = Bl(n, this.renderRoot, this.renderOptions);
  }
  connectedCallback() {
    super.connectedCallback(), this._$Do?.setConnected(!0);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this._$Do?.setConnected(!1);
  }
  render() {
    return pe;
  }
};
ht._$litElement$ = !0, ht.finalized = !0, globalThis.litElementHydrateSupport?.({ LitElement: ht });
const Fl = globalThis.litElementPolyfillSupport;
Fl?.({ LitElement: ht });
(globalThis.litElementVersions ??= []).push("4.1.1");
class Hl extends Dl(ht) {
}
class Kl extends Hl {
  constructor() {
    super(...arguments), this.disposers = [];
  }
  /**
   * Creates a MobX reaction using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  reaction(t, n, r) {
    this.disposers.push(Zn(t, n, r));
  }
  /**
   * Creates a MobX autorun using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  autorun(t, n) {
    this.disposers.push(Ii(t, n));
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.disposers.forEach((t) => {
      t();
    }), this.disposers = [];
  }
}
const se = window.Vaadin.copilot._sectionPanelUiState;
if (!se)
  throw new Error("Tried to access copilot section panel ui state before it was initialized.");
let Ee = [];
const Fr = [];
function Hr(e) {
  e.init({
    addPanel: (t) => {
      se.addPanel(t);
    },
    send(t, n) {
      te(t, n);
    }
  });
}
function ql() {
  Ee.push(import("./copilot-log-plugin-C0RIQyZq.js")), Ee.push(import("./copilot-info-plugin-fvoDBKOG.js")), Ee.push(import("./copilot-features-plugin-B4ZBkXaU.js")), Ee.push(import("./copilot-feedback-plugin-DXJN-ety.js")), Ee.push(import("./copilot-shortcuts-plugin-C9MlEWv9.js"));
}
function Wl() {
  {
    const e = `https://cdn.vaadin.com/copilot/${Xs}/copilot-plugins.js`;
    import(
      /* @vite-ignore */
      e
    ).catch((t) => {
      console.warn(`Unable to load plugins from ${e}. Some Copilot features are unavailable.`, t);
    });
  }
}
function Gl() {
  Promise.all(Ee).then(() => {
    const e = window.Vaadin;
    if (e.copilot.plugins) {
      const t = e.copilot.plugins;
      e.copilot.plugins.push = (n) => Hr(n), Array.from(t).forEach((n) => {
        Fr.includes(n) || (Hr(n), Fr.push(n));
      });
    }
  }), Ee = [];
}
function Su(e) {
  return Object.assign({
    expanded: !0,
    expandable: !1,
    panelOrder: 0,
    floating: !1,
    width: 500,
    height: 500,
    floatingPosition: {
      top: 50,
      left: 350
    }
  }, e);
}
function it() {
  return document.body.querySelector("copilot-main");
}
class Jl {
  constructor() {
    this.active = !1, this.activate = () => {
      this.active = !0, it()?.focus(), it()?.addEventListener("focusout", this.keepFocusInCopilot);
    }, this.deactivate = () => {
      this.active = !1, it()?.removeEventListener("focusout", this.keepFocusInCopilot);
    }, this.focusInEventListener = (t) => {
      this.active && (t.preventDefault(), t.stopPropagation(), qe(t.target) || requestAnimationFrame(() => {
        t.target.blur && t.target.blur(), it()?.focus();
      }));
    };
  }
  hostConnectedCallback() {
    const t = this.getApplicationRootElement();
    t && t instanceof HTMLElement && t.addEventListener("focusin", this.focusInEventListener);
  }
  hostDisconnectedCallback() {
    const t = this.getApplicationRootElement();
    t && t instanceof HTMLElement && t.removeEventListener("focusin", this.focusInEventListener);
  }
  getApplicationRootElement() {
    return document.body.firstElementChild;
  }
  keepFocusInCopilot(t) {
    t.preventDefault(), t.stopPropagation(), it()?.focus();
  }
}
const It = new Jl(), y = window.Vaadin.copilot.eventbus;
if (!y)
  throw new Error("Tried to access copilot eventbus before it was initialized.");
const ot = window.Vaadin.copilot.overlayManager, xu = {
  DragAndDrop: "Drag and Drop",
  RedoUndo: "Redo/Undo"
}, g = window.Vaadin.copilot._uiState;
if (!g)
  throw new Error("Tried to access copilot ui state before it was initialized.");
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const mo = { CHILD: 2, ELEMENT: 6 }, bo = (e) => (...t) => ({ _$litDirective$: e, values: t });
class _o {
  constructor(t) {
  }
  get _$AU() {
    return this._$AM._$AU;
  }
  _$AT(t, n, r) {
    this._$Ct = t, this._$AM = n, this._$Ci = r;
  }
  _$AS(t, n) {
    return this.update(t, n);
  }
  update(t, n) {
    return this.render(...n);
  }
}
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
class Bn extends _o {
  constructor(t) {
    if (super(t), this.it = O, t.type !== mo.CHILD) throw Error(this.constructor.directiveName + "() can only be used in child bindings");
  }
  render(t) {
    if (t === O || t == null) return this._t = void 0, this.it = t;
    if (t === pe) return t;
    if (typeof t != "string") throw Error(this.constructor.directiveName + "() called with a non-string value");
    if (t === this.it) return this._t;
    this.it = t;
    const n = [t];
    return n.raw = n, this._t = { _$litType$: this.constructor.resultType, strings: n, values: [] };
  }
}
Bn.directiveName = "unsafeHTML", Bn.resultType = 1;
const Xl = bo(Bn), Ct = window.Vaadin.copilot._machineState;
if (!Ct)
  throw new Error("Trying to use stored machine state before it was initialized");
const Yl = 5e3;
let Kr = 1;
function yo(e) {
  g.notifications.includes(e) && (e.dontShowAgain && e.dismissId && Zl(e.dismissId), g.removeNotification(e), y.emit("notification-dismissed", e));
}
function wo(e) {
  return Ct.getDismissedNotifications().includes(e);
}
function Zl(e) {
  wo(e) || Ct.addDismissedNotification(e);
}
function Ql(e) {
  return !(e.dismissId && (wo(e.dismissId) || g.notifications.find((t) => t.dismissId === e.dismissId)));
}
function Eo(e) {
  Ql(e) && ec(e);
}
function ec(e) {
  const t = Kr;
  Kr += 1;
  const n = { ...e, id: t, dontShowAgain: !1, animatingOut: !1 };
  g.setNotifications([...g.notifications, n]), (e.delay || !e.link && !e.dismissId) && setTimeout(() => {
    yo(n);
  }, e.delay ?? Yl), y.emit("notification-shown", e);
}
const tc = /* @__PURE__ */ Object.freeze(/* @__PURE__ */ Object.defineProperty({
  __proto__: null,
  dismissNotification: yo,
  showNotification: Eo
}, Symbol.toStringTag, { value: "Module" }));
function nc() {
  return g.idePluginState?.supportedActions?.find((e) => e === "restartApplication");
}
function rc() {
  hn(`${ve}plugin-restart-application`, {}, () => {
  }).catch((e) => {
    le("Error restarting server", e);
  });
}
const Oo = window.Vaadin.copilot._previewState;
if (!Oo)
  throw new Error("Tried to access copilot preview state before it was initialized.");
const ic = async () => eo(() => g.userInfo), Nu = async () => (await ic()).vaadiner;
function oc() {
  const e = g.userInfo;
  return !e || e.copilotProjectCannotLeaveLocalhost ? !1 : Ct.isSendErrorReportsAllowed();
}
const ac = (e) => {
  le("Unspecified error", e), y.emit("vite-after-update", {});
}, sc = (e, t) => e.error ? (lc(e.error, t), !0) : !1, Ao = (e, t, n, r) => {
  vn({
    type: Ve.ERROR,
    message: e,
    details: to(
      Ue`<vaadin-details summary="Details" style="color: var(--dev-tools-text-color)"
        ><div>
          <code class="codeblock" style="white-space: normal;color: var(--color)"
            ><copilot-copy></copilot-copy>${Xl(t)}</code
          >
          <vaadin-button hidden>Report this issue</vaadin-button>
        </div></vaadin-details
      >`
    ),
    delay: 3e4
  }), oc() && y.emit("system-info-with-callback", {
    callback: (i) => y.send("copilot-error", {
      message: e,
      details: String(n).replace("	", `
`) + (r ? `
 
Request: 
${JSON.stringify(r)}
` : ""),
      versions: i
    }),
    notify: !1
  }), g.clearOperationWaitsHmrUpdate();
}, lc = (e, t) => {
  Ao(
    e.message,
    e.exceptionMessage ?? "",
    e.exceptionStacktrace?.join(`
`) ?? "",
    t
  );
};
function cc(e, t) {
  Ao(e, t.message, t.stack || "");
}
function Sn(e) {
  const t = Object.keys(e);
  return t.length === 1 && t.includes("message") || t.length >= 3 && t.includes("message") && t.includes("exceptionMessage") && t.includes("exceptionStacktrace");
}
function le(e, t) {
  const n = Sn(t) ? t.exceptionMessage ?? t.message : t, r = {
    type: Ve.ERROR,
    message: "Copilot internal error",
    details: e + (n ? `
${n}` : "")
  };
  Sn(t) && t.suggestRestart && nc() && (r.details = to(
    Ue`${e}<br />${n}
        <button
          style="align-self:start;padding:0"
          @click=${(o) => {
      const a = o.target;
      a.disabled = !0, a.innerText = "Restarting...", rc();
    }}>
          Restart now
        </button>`
  ), r.delay = 3e4), vn(r);
  let i;
  t instanceof Error ? i = t.stack : Sn(t) ? i = t?.exceptionStacktrace?.join(`
`) : i = t?.toString(), y.emit("system-info-with-callback", {
    callback: (o) => y.send("copilot-error", {
      message: `Copilot internal error: ${e}`,
      details: i,
      versions: o
    }),
    notify: !1
  });
}
function qr(e) {
  return e?.stack?.includes("cdn.vaadin.com/copilot") || e?.stack?.includes("/copilot/copilot/") || e?.stack?.includes("/copilot/copilot-private/");
}
function So() {
  const e = window.onerror;
  window.onerror = (n, r, i, o, a) => {
    if (qr(a)) {
      le(n.toString(), a);
      return;
    }
    e && e(n, r, i, o, a);
  }, Ka((n) => {
    qr(n) && le("", n);
  });
  const t = window.Vaadin.ConsoleErrors;
  Array.isArray(t) && Fn.push(...t), xo((n) => Fn.push(n));
}
const Fn = [];
function xo(e) {
  const t = window.Vaadin.ConsoleErrors;
  window.Vaadin.ConsoleErrors = {
    push: (n) => {
      n[0].type !== void 0 && n[0].message !== void 0 ? e({
        type: n[0].type,
        message: n[0].message,
        internal: !!n[0].internal,
        details: n[0].details,
        link: n[0].link
      }) : e({ type: Ve.ERROR, message: n.map((r) => uc(r)).join(" "), internal: !1 }), t.push(n);
    }
  };
}
function uc(e) {
  return e.message ? e.message.toString() : e.toString();
}
function dc(e) {
  vn({
    type: Ve.ERROR,
    message: `Unable to ${e}`,
    details: "Could not find sources for React components, probably because the project is not a React (or Flow) project"
  });
}
const fc = /* @__PURE__ */ Object.freeze(/* @__PURE__ */ Object.defineProperty({
  __proto__: null,
  catchErrors: xo,
  consoleErrorsQueue: Fn,
  handleBrowserOperationError: cc,
  handleCopilotError: le,
  handleErrorDuringOperation: ac,
  handleServerOperationErrorIfNeeded: sc,
  installErrorHandlers: So,
  showNotReactFlowProject: dc
}, Symbol.toStringTag, { value: "Module" })), No = () => {
  hc().then((e) => g.setUserInfo(e)).catch((e) => le("Failed to load userInfo", e));
}, hc = async () => hn(`${ve}get-user-info`, {}, (e) => (delete e.data.reqId, e.data));
y.on("copilot-prokey-received", (e) => {
  No(), e.preventDefault();
});
/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const Co = Symbol.for(""), vc = (e) => {
  if (e?.r === Co) return e?._$litStatic$;
}, Po = (e) => ({ _$litStatic$: e, r: Co }), Wr = /* @__PURE__ */ new Map(), pc = (e) => (t, ...n) => {
  const r = n.length;
  let i, o;
  const a = [], s = [];
  let l, c = 0, u = !1;
  for (; c < r; ) {
    for (l = t[c]; c < r && (o = n[c], (i = vc(o)) !== void 0); ) l += i + t[++c], u = !0;
    c !== r && s.push(o), a.push(l), c++;
  }
  if (c === r && a.push(t[r]), u) {
    const d = a.join("$$lit$$");
    (t = Wr.get(d)) === void 0 && (a.raw = a, Wr.set(d, t = a)), n = s;
  }
  return e(t, ...n);
}, vt = pc(Ue);
/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const { I: gc } = zl, Cu = (e) => e.strings === void 0, Gr = () => document.createComment(""), at = (e, t, n) => {
  const r = e._$AA.parentNode, i = t === void 0 ? e._$AB : t._$AA;
  if (n === void 0) {
    const o = r.insertBefore(Gr(), i), a = r.insertBefore(Gr(), i);
    n = new gc(o, a, e, e.options);
  } else {
    const o = n._$AB.nextSibling, a = n._$AM, s = a !== e;
    if (s) {
      let l;
      n._$AQ?.(e), n._$AM = e, n._$AP !== void 0 && (l = e._$AU) !== a._$AU && n._$AP(l);
    }
    if (o !== i || s) {
      let l = n._$AA;
      for (; l !== o; ) {
        const c = l.nextSibling;
        r.insertBefore(l, i), l = c;
      }
    }
  }
  return n;
}, we = (e, t, n = e) => (e._$AI(t, n), e), mc = {}, bc = (e, t = mc) => e._$AH = t, _c = (e) => e._$AH, xn = (e) => {
  e._$AP?.(!1, !0);
  let t = e._$AA;
  const n = e._$AB.nextSibling;
  for (; t !== n; ) {
    const r = t.nextSibling;
    t.remove(), t = r;
  }
};
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const Jr = (e, t, n) => {
  const r = /* @__PURE__ */ new Map();
  for (let i = t; i <= n; i++) r.set(e[i], i);
  return r;
}, $o = bo(class extends _o {
  constructor(e) {
    if (super(e), e.type !== mo.CHILD) throw Error("repeat() can only be used in text expressions");
  }
  dt(e, t, n) {
    let r;
    n === void 0 ? n = t : t !== void 0 && (r = t);
    const i = [], o = [];
    let a = 0;
    for (const s of e) i[a] = r ? r(s, a) : a, o[a] = n(s, a), a++;
    return { values: o, keys: i };
  }
  render(e, t, n) {
    return this.dt(e, t, n).values;
  }
  update(e, [t, n, r]) {
    const i = _c(e), { values: o, keys: a } = this.dt(t, n, r);
    if (!Array.isArray(i)) return this.ut = a, o;
    const s = this.ut ??= [], l = [];
    let c, u, d = 0, v = i.length - 1, p = 0, b = o.length - 1;
    for (; d <= v && p <= b; ) if (i[d] === null) d++;
    else if (i[v] === null) v--;
    else if (s[d] === a[p]) l[p] = we(i[d], o[p]), d++, p++;
    else if (s[v] === a[b]) l[b] = we(i[v], o[b]), v--, b--;
    else if (s[d] === a[b]) l[b] = we(i[d], o[b]), at(e, l[b + 1], i[d]), d++, b--;
    else if (s[v] === a[p]) l[p] = we(i[v], o[p]), at(e, i[d], i[v]), v--, p++;
    else if (c === void 0 && (c = Jr(a, p, b), u = Jr(s, d, v)), c.has(s[d])) if (c.has(s[v])) {
      const E = u.get(a[p]), x = E !== void 0 ? i[E] : null;
      if (x === null) {
        const G = at(e, i[d]);
        we(G, o[p]), l[p] = G;
      } else l[p] = we(x, o[p]), at(e, i[d], x), i[E] = null;
      p++;
    } else xn(i[v]), v--;
    else xn(i[d]), d++;
    for (; p <= b; ) {
      const E = at(e, l[b + 1]);
      we(E, o[p]), l[p++] = E;
    }
    for (; d <= v; ) {
      const E = i[d++];
      E !== null && xn(E);
    }
    return this.ut = a, bc(e, l), pe;
  }
}), Lt = /* @__PURE__ */ new Map(), yc = (e) => {
  const n = se.panels.filter((r) => !r.floating && r.panel === e).sort((r, i) => r.panelOrder - i.panelOrder);
  return vt`
    ${$o(
    n,
    (r) => r.tag,
    (r) => {
      const i = Po(r.tag);
      return vt` <copilot-section-panel-wrapper panelTag="${i}">
          ${se.shouldRender(r.tag) ? vt`<${i} slot="content"></${i}>` : O}
        </copilot-section-panel-wrapper>`;
    }
  )}
  `;
}, wc = () => {
  const e = se.panels;
  return vt`
    ${$o(
    e.filter((t) => t.floating),
    (t) => t.tag,
    (t) => {
      const n = Po(t.tag);
      return vt`
                        <copilot-section-panel-wrapper panelTag="${n}">
                            <${n} slot="content"></${n}>
                        </copilot-section-panel-wrapper>`;
    }
  )}
  `;
}, Pu = (e) => {
  const t = e.panelTag, n = e.querySelector('[slot="content"]');
  n && Lt.set(t, n);
}, $u = (e) => {
  if (Lt.has(e.panelTag)) {
    const t = Lt.get(e.panelTag);
    e.querySelector('[slot="content"]').replaceWith(t);
  }
  Lt.delete(e.panelTag);
}, N = [];
for (let e = 0; e < 256; ++e)
  N.push((e + 256).toString(16).slice(1));
function Ec(e, t = 0) {
  return (N[e[t + 0]] + N[e[t + 1]] + N[e[t + 2]] + N[e[t + 3]] + "-" + N[e[t + 4]] + N[e[t + 5]] + "-" + N[e[t + 6]] + N[e[t + 7]] + "-" + N[e[t + 8]] + N[e[t + 9]] + "-" + N[e[t + 10]] + N[e[t + 11]] + N[e[t + 12]] + N[e[t + 13]] + N[e[t + 14]] + N[e[t + 15]]).toLowerCase();
}
let Nn;
const Oc = new Uint8Array(16);
function Ac() {
  if (!Nn) {
    if (typeof crypto > "u" || !crypto.getRandomValues)
      throw new Error("crypto.getRandomValues() not supported. See https://github.com/uuidjs/uuid#getrandomvalues-not-supported");
    Nn = crypto.getRandomValues.bind(crypto);
  }
  return Nn(Oc);
}
const Sc = typeof crypto < "u" && crypto.randomUUID && crypto.randomUUID.bind(crypto), Xr = { randomUUID: Sc };
function Do(e, t, n) {
  if (Xr.randomUUID && !e)
    return Xr.randomUUID();
  e = e || {};
  const r = e.random ?? e.rng?.() ?? Ac();
  if (r.length < 16)
    throw new Error("Random bytes length must be >= 16");
  return r[6] = r[6] & 15 | 64, r[8] = r[8] & 63 | 128, Ec(r);
}
const zt = [], ut = [], Du = async (e, t, n) => {
  let r, i;
  t.reqId = Do();
  const o = new Promise((a, s) => {
    r = a, i = s;
  });
  return zt.push({
    handleMessage(a) {
      if (a?.data?.reqId !== t.reqId)
        return !1;
      try {
        r(n(a));
      } catch (s) {
        i(s);
      }
      return !0;
    }
  }), te(e, t), o;
};
function xc(e) {
  for (const t of zt)
    if (t.handleMessage(e))
      return zt.splice(zt.indexOf(t), 1), !0;
  if (y.emitUnsafe({ type: e.command, data: e.data }))
    return !0;
  for (const t of ko())
    if (To(t, e))
      return !0;
  return ut.push(e), !1;
}
function To(e, t) {
  return e.handleMessage?.call(e, t);
}
function Nc() {
  if (ut.length)
    for (const e of ko())
      for (let t = 0; t < ut.length; t++)
        To(e, ut[t]) && (ut.splice(t, 1), t--);
}
function ko() {
  const e = document.querySelector("copilot-main");
  return e ? e.renderRoot.querySelectorAll("copilot-section-panel-wrapper *") : [];
}
const Cc = "@keyframes bounce{0%{transform:scale(.8)}50%{transform:scale(1.5)}to{transform:scale(1)}}@keyframes around-we-go-again{0%{background-position:0 0,0 0,calc(var(--glow-size) * -.5) calc(var(--glow-size) * -.5),calc(100% + calc(var(--glow-size) * .5)) calc(100% + calc(var(--glow-size) * .5))}25%{background-position:0 0,0 0,calc(100% + calc(var(--glow-size) * .5)) calc(var(--glow-size) * -.5),calc(var(--glow-size) * -.5) calc(100% + calc(var(--glow-size) * .5))}50%{background-position:0 0,0 0,calc(100% + calc(var(--glow-size) * .5)) calc(100% + calc(var(--glow-size) * .5)),calc(var(--glow-size) * -.5) calc(var(--glow-size) * -.5)}75%{background-position:0 0,0 0,calc(var(--glow-size) * -.5) calc(100% + calc(var(--glow-size) * .5)),calc(100% + calc(var(--glow-size) * .5)) calc(var(--glow-size) * -.5)}to{background-position:0 0,0 0,calc(var(--glow-size) * -.5) calc(var(--glow-size) * -.5),calc(100% + calc(var(--glow-size) * .5)) calc(100% + calc(var(--glow-size) * .5))}}@keyframes swirl{0%{rotate:0deg;filter:hue-rotate(20deg)}50%{filter:hue-rotate(-30deg)}to{rotate:360deg;filter:hue-rotate(20deg)}}@keyframes button-focus-in{0%{box-shadow:0 0 0 0 var(--focus-color)}to{box-shadow:0 0 0 var(--focus-size) var(--focus-color)}}@keyframes button-focus-out{0%{box-shadow:0 0 0 var(--focus-size) var(--focus-color)}}@keyframes link-focus-in{0%{box-shadow:0 0 0 0 var(--blue-color)}to{box-shadow:0 0 0 var(--focus-size) var(--blue-color)}}@keyframes link-focus-out{0%{box-shadow:0 0 0 var(--focus-size) var(--blue-color)}}@keyframes ping{75%,to{transform:scale(2);opacity:0}}", Pc = ":host{--gray-h: 220;--gray-s: 30%;--gray-l: 30%;--gray-hsl: var(--gray-h) var(--gray-s) var(--gray-l);--gray: hsl(var(--gray-hsl));--gray-50: hsl(var(--gray-hsl) / .05);--gray-100: hsl(var(--gray-hsl) / .1);--gray-150: hsl(var(--gray-hsl) / .16);--gray-200: hsl(var(--gray-hsl) / .24);--gray-250: hsl(var(--gray-hsl) / .34);--gray-300: hsl(var(--gray-hsl) / .46);--gray-350: hsl(var(--gray-hsl) / .6);--gray-400: hsl(var(--gray-hsl) / .7);--gray-450: hsl(var(--gray-hsl) / .8);--gray-500: hsl(var(--gray-hsl) / .9);--gray-550: hsl(var(--gray-hsl));--gray-600: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 2%));--gray-650: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 4%));--gray-700: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 8%));--gray-750: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 12%));--gray-800: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 20%));--gray-850: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 23%));--gray-900: hsl(var(--gray-h) var(--gray-s) calc(var(--gray-l) - 30%));--blue-h: 220;--blue-s: 90%;--blue-l: 53%;--blue-hsl: var(--blue-h) var(--blue-s) var(--blue-l);--blue: hsl(var(--blue-hsl));--blue-50: hsl(var(--blue-hsl) / .05);--blue-100: hsl(var(--blue-hsl) / .1);--blue-150: hsl(var(--blue-hsl) / .2);--blue-200: hsl(var(--blue-hsl) / .3);--blue-250: hsl(var(--blue-hsl) / .4);--blue-300: hsl(var(--blue-hsl) / .5);--blue-350: hsl(var(--blue-hsl) / .6);--blue-400: hsl(var(--blue-hsl) / .7);--blue-450: hsl(var(--blue-hsl) / .8);--blue-500: hsl(var(--blue-hsl) / .9);--blue-550: hsl(var(--blue-hsl));--blue-600: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 4%));--blue-650: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 8%));--blue-700: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 12%));--blue-750: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 15%));--blue-800: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 18%));--blue-850: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 24%));--blue-900: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) - 27%));--purple-h: 246;--purple-s: 90%;--purple-l: 60%;--purple-hsl: var(--purple-h) var(--purple-s) var(--purple-l);--purple: hsl(var(--purple-hsl));--purple-50: hsl(var(--purple-hsl) / .05);--purple-100: hsl(var(--purple-hsl) / .1);--purple-150: hsl(var(--purple-hsl) / .2);--purple-200: hsl(var(--purple-hsl) / .3);--purple-250: hsl(var(--purple-hsl) / .4);--purple-300: hsl(var(--purple-hsl) / .5);--purple-350: hsl(var(--purple-hsl) / .6);--purple-400: hsl(var(--purple-hsl) / .7);--purple-450: hsl(var(--purple-hsl) / .8);--purple-500: hsl(var(--purple-hsl) / .9);--purple-550: hsl(var(--purple-hsl));--purple-600: hsl(var(--purple-h) calc(var(--purple-s) - 4%) calc(var(--purple-l) - 2%));--purple-650: hsl(var(--purple-h) calc(var(--purple-s) - 8%) calc(var(--purple-l) - 4%));--purple-700: hsl(var(--purple-h) calc(var(--purple-s) - 15%) calc(var(--purple-l) - 7%));--purple-750: hsl(var(--purple-h) calc(var(--purple-s) - 23%) calc(var(--purple-l) - 11%));--purple-800: hsl(var(--purple-h) calc(var(--purple-s) - 24%) calc(var(--purple-l) - 15%));--purple-850: hsl(var(--purple-h) calc(var(--purple-s) - 24%) calc(var(--purple-l) - 19%));--purple-900: hsl(var(--purple-h) calc(var(--purple-s) - 27%) calc(var(--purple-l) - 23%));--green-h: 150;--green-s: 80%;--green-l: 42%;--green-hsl: var(--green-h) var(--green-s) var(--green-l);--green: hsl(var(--green-hsl));--green-50: hsl(var(--green-hsl) / .05);--green-100: hsl(var(--green-hsl) / .1);--green-150: hsl(var(--green-hsl) / .2);--green-200: hsl(var(--green-hsl) / .3);--green-250: hsl(var(--green-hsl) / .4);--green-300: hsl(var(--green-hsl) / .5);--green-350: hsl(var(--green-hsl) / .6);--green-400: hsl(var(--green-hsl) / .7);--green-450: hsl(var(--green-hsl) / .8);--green-500: hsl(var(--green-hsl) / .9);--green-550: hsl(var(--green-hsl));--green-600: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 2%));--green-650: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 4%));--green-700: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 8%));--green-750: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 12%));--green-800: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 15%));--green-850: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 19%));--green-900: hsl(var(--green-h) var(--green-s) calc(var(--green-l) - 23%));--yellow-h: 38;--yellow-s: 98%;--yellow-l: 64%;--yellow-hsl: var(--yellow-h) var(--yellow-s) var(--yellow-l);--yellow: hsl(var(--yellow-hsl));--yellow-50: hsl(var(--yellow-hsl) / .07);--yellow-100: hsl(var(--yellow-hsl) / .12);--yellow-150: hsl(var(--yellow-hsl) / .2);--yellow-200: hsl(var(--yellow-hsl) / .3);--yellow-250: hsl(var(--yellow-hsl) / .4);--yellow-300: hsl(var(--yellow-hsl) / .5);--yellow-350: hsl(var(--yellow-hsl) / .6);--yellow-400: hsl(var(--yellow-hsl) / .7);--yellow-450: hsl(var(--yellow-hsl) / .8);--yellow-500: hsl(var(--yellow-hsl) / .9);--yellow-550: hsl(var(--yellow-hsl));--yellow-600: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 5%));--yellow-650: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 10%));--yellow-700: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 15%));--yellow-750: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 20%));--yellow-800: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 25%));--yellow-850: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 30%));--yellow-900: hsl(var(--yellow-h) var(--yellow-s) calc(var(--yellow-l) - 35%));--red-h: 355;--red-s: 75%;--red-l: 55%;--red-hsl: var(--red-h) var(--red-s) var(--red-l);--red: hsl(var(--red-hsl));--red-50: hsl(var(--red-hsl) / .05);--red-100: hsl(var(--red-hsl) / .1);--red-150: hsl(var(--red-hsl) / .2);--red-200: hsl(var(--red-hsl) / .3);--red-250: hsl(var(--red-hsl) / .4);--red-300: hsl(var(--red-hsl) / .5);--red-350: hsl(var(--red-hsl) / .6);--red-400: hsl(var(--red-hsl) / .7);--red-450: hsl(var(--red-hsl) / .8);--red-500: hsl(var(--red-hsl) / .9);--red-550: hsl(var(--red-hsl));--red-600: hsl(var(--red-h) calc(var(--red-s) - 5%) calc(var(--red-l) - 2%));--red-650: hsl(var(--red-h) calc(var(--red-s) - 10%) calc(var(--red-l) - 4%));--red-700: hsl(var(--red-h) calc(var(--red-s) - 15%) calc(var(--red-l) - 8%));--red-750: hsl(var(--red-h) calc(var(--red-s) - 20%) calc(var(--red-l) - 12%));--red-800: hsl(var(--red-h) calc(var(--red-s) - 25%) calc(var(--red-l) - 15%));--red-850: hsl(var(--red-h) calc(var(--red-s) - 30%) calc(var(--red-l) - 19%));--red-900: hsl(var(--red-h) calc(var(--red-s) - 35%) calc(var(--red-l) - 23%));--codeblock-bg: #f4f4f4;--background-color: rgba(255, 255, 255, .87);--primary-color: #0368de;--input-border-color: rgba(0, 0, 0, .42);--divider-primary-color: rgba(0, 0, 0, .1);--divider-secondary-color: rgba(0, 0, 0, .05);--switch-active-color: #0b754f;--switch-inactive-color: #666666;--body-text-color: rgba(0, 0, 0, .87);--secondary-text-color: rgba(0, 0, 0, .6);--primary-contrast-text-color: white;--active-color: rgba(3, 104, 222, .1);--focus-color: #0377ff;--hover-color: rgba(0, 0, 0, .05);--success-color: var(--success-color-80);--error-color: var(--error-color-70);--warning-color: #8a6c1e;--success-color-5: #f0fffa;--success-color-10: #eafaf4;--success-color-20: #d2f0e5;--success-color-30: #8ce4c5;--success-color-40: #39c693;--success-color-50: #1ba875;--success-color-60: #0e9c69;--success-color-70: #0d8b5e;--success-color-80: #066845;--success-color-90: #004d31;--error-color-5: #fff5f6;--error-color-10: #ffedee;--error-color-20: #ffd0d4;--error-color-30: #f8a8ae;--error-color-40: #ff707a;--error-color-50: #ff3a49;--error-color-60: #ff0013;--error-color-70: #ce0010;--error-color-80: #97000b;--error-color-90: #680008;--contrast-color-5: rgba(0, 0, 0, .05);--contrast-color-10: rgba(0, 0, 0, .1);--contrast-color-20: rgba(0, 0, 0, .2);--contrast-color-30: rgba(0, 0, 0, .3);--contrast-color-40: rgba(0, 0, 0, .4);--contrast-color-50: rgba(0, 0, 0, .5);--contrast-color-60: rgba(0, 0, 0, .6);--contrast-color-70: rgba(0, 0, 0, .7);--contrast-color-80: rgba(0, 0, 0, .8);--contrast-color-90: rgba(0, 0, 0, .9);--contrast-color-100: black;--blue-color: #0368de;--violet-color: #7b2bff}:host(.dark){--gray-s: 15%;--gray-l: 70%;--gray-600: hsl(var(--gray-h) calc(var(--gray-s) - 2%) calc(var(--gray-l) + 6%));--gray-650: hsl(var(--gray-h) calc(var(--gray-s) - 5%) calc(var(--gray-l) + 14%));--gray-700: hsl(var(--gray-h) calc(var(--gray-s) - 2%) calc(var(--gray-l) + 26%));--gray-750: hsl(var(--gray-h) calc(var(--gray-s) - 2%) calc(var(--gray-l) + 36%));--gray-800: hsl(var(--gray-h) calc(var(--gray-s) - 2%) calc(var(--gray-l) + 48%));--gray-850: hsl(var(--gray-h) calc(var(--gray-s) - 2%) calc(var(--gray-l) + 62%));--gray-900: hsl(var(--gray-h) calc(var(--gray-s) - 2%) calc(var(--gray-l) + 70%));--blue-s: 90%;--blue-l: 58%;--blue-600: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 6%));--blue-650: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 12%));--blue-700: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 17%));--blue-750: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 22%));--blue-800: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 28%));--blue-850: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 35%));--blue-900: hsl(var(--blue-h) var(--blue-s) calc(var(--blue-l) + 43%));--purple-600: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 4%));--purple-650: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 9%));--purple-700: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 12%));--purple-750: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 18%));--purple-800: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 24%));--purple-850: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 29%));--purple-900: hsl(var(--purple-h) var(--purple-s) calc(var(--purple-l) + 33%));--green-600: hsl(calc(var(--green-h) - 1) calc(var(--green-s) - 5%) calc(var(--green-l) + 5%));--green-650: hsl(calc(var(--green-h) - 2) calc(var(--green-s) - 10%) calc(var(--green-l) + 12%));--green-700: hsl(calc(var(--green-h) - 4) calc(var(--green-s) - 15%) calc(var(--green-l) + 20%));--green-750: hsl(calc(var(--green-h) - 6) calc(var(--green-s) - 20%) calc(var(--green-l) + 29%));--green-800: hsl(calc(var(--green-h) - 8) calc(var(--green-s) - 25%) calc(var(--green-l) + 37%));--green-850: hsl(calc(var(--green-h) - 10) calc(var(--green-s) - 30%) calc(var(--green-l) + 42%));--green-900: hsl(calc(var(--green-h) - 12) calc(var(--green-s) - 35%) calc(var(--green-l) + 48%));--yellow-600: hsl(calc(var(--yellow-h) + 1) var(--yellow-s) calc(var(--yellow-l) + 4%));--yellow-650: hsl(calc(var(--yellow-h) + 2) var(--yellow-s) calc(var(--yellow-l) + 7%));--yellow-700: hsl(calc(var(--yellow-h) + 4) var(--yellow-s) calc(var(--yellow-l) + 11%));--yellow-750: hsl(calc(var(--yellow-h) + 6) var(--yellow-s) calc(var(--yellow-l) + 16%));--yellow-800: hsl(calc(var(--yellow-h) + 8) var(--yellow-s) calc(var(--yellow-l) + 20%));--yellow-850: hsl(calc(var(--yellow-h) + 10) var(--yellow-s) calc(var(--yellow-l) + 24%));--yellow-900: hsl(calc(var(--yellow-h) + 12) var(--yellow-s) calc(var(--yellow-l) + 29%));--red-600: hsl(calc(var(--red-h) - 1) calc(var(--red-s) - 5%) calc(var(--red-l) + 3%));--red-650: hsl(calc(var(--red-h) - 2) calc(var(--red-s) - 10%) calc(var(--red-l) + 7%));--red-700: hsl(calc(var(--red-h) - 4) calc(var(--red-s) - 15%) calc(var(--red-l) + 14%));--red-750: hsl(calc(var(--red-h) - 6) calc(var(--red-s) - 20%) calc(var(--red-l) + 19%));--red-800: hsl(calc(var(--red-h) - 8) calc(var(--red-s) - 25%) calc(var(--red-l) + 24%));--red-850: hsl(calc(var(--red-h) - 10) calc(var(--red-s) - 30%) calc(var(--red-l) + 30%));--red-900: hsl(calc(var(--red-h) - 12) calc(var(--red-s) - 35%) calc(var(--red-l) + 36%));--codeblock-bg: var(--gray-100);--background-color: rgba(0, 0, 0, .87);--primary-color: white;--input-border-color: rgba(255, 255, 255, .42);--divider-primary-color: rgba(255, 255, 255, .2);--divider-secondary-color: rgba(255, 255, 255, .1);--body-text-color: white;--secondary-text-color: rgba(255, 255, 255, .7);--primary-contrast-text-color: rgba(0, 0, 0, .87);--active-color: rgba(255, 255, 255, .15);--focus-color: rgba(255, 255, 255, .5);--hover-color: rgba(255, 255, 255, .1);--success-color: var(--success-color-50);--error-color: var(--error-color-50);--warning-color: #fec941;--success-color-5: #004d31;--success-color-10: #066845;--success-color-20: #0d8b5e;--success-color-30: #0e9c69;--success-color-40: #1ba875;--success-color-50: #39c693;--success-color-60: #8ce4c5;--success-color-70: #d2f0e5;--success-color-80: #eafaf4;--success-color-90: #f0fffa;--error-color-5: #680008;--error-color-10: #97000b;--error-color-20: #ce0010;--error-color-30: #ff0013;--error-color-40: #ff3a49;--error-color-50: #ff707a;--error-color-60: #f8a8ae;--error-color-70: #ffd0d4;--error-color-80: #ffedee;--error-color-90: #fff5f6;--contrast-color-5: rgba(255, 255, 255, .05);--contrast-color-10: rgba(255, 255, 255, .1);--contrast-color-20: rgba(255, 255, 255, .2);--contrast-color-30: rgba(255, 255, 255, .3);--contrast-color-40: rgba(255, 255, 255, .4);--contrast-color-50: rgba(255, 255, 255, .5);--contrast-color-60: rgba(255, 255, 255, .6);--contrast-color-70: rgba(255, 255, 255, .7);--contrast-color-80: rgba(255, 255, 255, .8);--contrast-color-90: rgba(255, 255, 255, .9);--contrast-color-100: white;--blue-color: #95c6ff;--violet-color: #cbb4ff}", $c = ':host{--font-family: "Manrope", sans-serif;--monospace-font-family: Inconsolata, Monaco, Consolas, Courier New, Courier, monospace;--font-size-0: .6875rem;--font-size-1: .75rem;--font-size-2: .875rem;--font-size-3: 1rem;--font-size-4: 1.125rem;--font-size-5: 1.25rem;--font-size-6: 1.375rem;--font-size-7: 1.5rem;--line-height-0: 1rem;--line-height-1: 1.125rem;--line-height-2: 1.25rem;--line-height-3: 1.5rem;--line-height-4: 1.75rem;--line-height-5: 2rem;--line-height-6: 2.25rem;--line-height-7: 2.5rem;--font-weight-normal: 440;--font-weight-medium: 540;--font-weight-semibold: 640;--font-weight-bold: 740;--font: normal var(--font-weight-normal) var(--font-size-3) / var(--line-height-3) var(--font-family);--font-medium: normal var(--font-weight-medium) var(--font-size-3) / var(--line-height-3) var(--font-family);--font-semibold: normal var(--font-weight-semibold) var(--font-size-3) / var(--line-height-3) var(--font-family);--font-bold: normal var(--font-weight-bold) var(--font-size-3) / var(--line-height-3) var(--font-family);--font-small: normal var(--font-weight-normal) var(--font-size-2) / var(--line-height-2) var(--font-family);--font-small-medium: normal var(--font-weight-medium) var(--font-size-2) / var(--line-height-2) var(--font-family);--font-small-semibold: normal var(--font-weight-semibold) var(--font-size-2) / var(--line-height-2) var(--font-family);--font-small-bold: normal var(--font-weight-bold) var(--font-size-2) / var(--line-height-2) var(--font-family);--font-xsmall: normal var(--font-weight-normal) var(--font-size-1) / var(--line-height-1) var(--font-family);--font-xsmall-medium: normal var(--font-weight-medium) var(--font-size-1) / var(--line-height-1) var(--font-family);--font-xsmall-semibold: normal var(--font-weight-semibold) var(--font-size-1) / var(--line-height-1) var(--font-family);--font-xsmall-bold: normal var(--font-weight-bold) var(--font-size-1) / var(--line-height-1) var(--font-family);--font-xxsmall: normal var(--font-weight-normal) var(--font-size-0) / var(--line-height-0) var(--font-family);--font-xxsmall-medium: normal var(--font-weight-medium) var(--font-size-0) / var(--line-height-0) var(--font-family);--font-xxsmall-semibold: normal var(--font-weight-semibold) var(--font-size-0) / var(--line-height-0) var(--font-family);--font-xxsmall-bold: normal var(--font-weight-bold) var(--font-size-0) / var(--line-height-0) var(--font-family);--font-button: normal var(--font-weight-semibold) var(--font-size-1) / var(--line-height-1) var(--font-family);--font-tooltip: normal var(--font-weight-medium) var(--font-size-1) / var(--line-height-2) var(--font-family);--z-index-component-selector: 100;--z-index-floating-panel: 101;--z-index-drawer: 150;--z-index-opened-drawer: 151;--z-index-spotlight: 200;--z-index-popover: 300;--z-index-activation-button: 1000;--duration-1: .1s;--duration-2: .2s;--duration-3: .3s;--duration-4: .4s;--button-background: var(--gray-100);--button-background-hover: var(--gray-150);--focus-size: 2px;--icon-size-xs: .75rem;--icon-size-s: 1rem;--icon-size-m: 1.125rem;--shadow-xs: 0 1px 2px 0 rgb(0 0 0 / .05);--shadow-s: 0 1px 3px 0 rgb(0 0 0 / .1), 0 1px 2px -1px rgb(0 0 0 / .1);--shadow-m: 0 4px 6px -1px rgb(0 0 0 / .1), 0 2px 4px -2px rgb(0 0 0 / .1);--shadow-l: 0 10px 15px -3px rgb(0 0 0 / .1), 0 4px 6px -4px rgb(0 0 0 / .1);--shadow-xl: 0 20px 25px -5px rgb(0 0 0 / .1), 0 8px 10px -6px rgb(0 0 0 / .1);--shadow-2xl: 0 25px 50px -12px rgb(0 0 0 / .25);--size-xs: 1.25rem;--size-s: 1.5rem;--size-m: 1.75rem;--size-l: 2rem;--size-xl: 2.25rem;--space-25: 2px;--space-50: 4px;--space-75: 6px;--space-100: 8px;--space-150: 12px;--space-200: 16px;--space-300: 24px;--space-400: 32px;--space-500: 40px;--space-600: 48px;--space-700: 56px;--space-800: 64px;--space-900: 72px;--radius-1: .1875rem;--radius-2: .375rem;--radius-3: .75rem}:host{--lumo-font-family: var(--font-family);--lumo-font-size-xs: var(--font-size-1);--lumo-font-size-s: var(--font-size-2);--lumo-font-size-l: var(--font-size-4);--lumo-font-size-xl: var(--font-size-5);--lumo-font-size-xxl: var(--font-size-6);--lumo-font-size-xxxl: var(--font-size-7);--lumo-line-height-s: var(--line-height-2);--lumo-line-height-m: var(--line-height-3);--lumo-line-height-l: var(--line-height-4);--lumo-border-radius-s: var(--radius-1);--lumo-border-radius-m: var(--radius-2);--lumo-border-radius-l: var(--radius-3);--lumo-base-color: var(--surface-0);--lumo-header-text-color: var(--color-high-contrast);--lumo-tertiary-text-color: var(--color);--lumo-primary-text-color: var(--color-high-contrast);--lumo-primary-color: var(--color-high-contrast);--lumo-primary-color-50pct: var(--color-accent);--lumo-primary-contrast-color: var(--lumo-secondary-text-color);--lumo-space-xs: var(--space-50);--lumo-space-s: var(--space-100);--lumo-space-m: var(--space-200);--lumo-space-l: var(--space-300);--lumo-space-xl: var(--space-500);--lumo-icon-size-xs: var(--font-size-1);--lumo-icon-size-s: var(--font-size-2);--lumo-icon-size-m: var(--font-size-3);--lumo-icon-size-l: var(--font-size-4);--lumo-icon-size-xl: var(--font-size-5);--vaadin-focus-ring-color: var(--focus-color);--vaadin-focus-ring-width: var(--focus-size);--lumo-font-size-m: var(--font-size-1);--lumo-body-text-color: var(--body-text-color);--lumo-secondary-text-color: var(--secondary-text-color);--lumo-error-text-color: var(--error-color);--lumo-size-m: var(--size-m)}:host{color-scheme:light;--surface-0: hsl(var(--gray-h) var(--gray-s) 90% / .8);--surface-1: hsl(var(--gray-h) var(--gray-s) 95% / .8);--surface-2: hsl(var(--gray-h) var(--gray-s) 100% / .8);--surface-background: linear-gradient( hsl(var(--gray-h) var(--gray-s) 95% / .7), hsl(var(--gray-h) var(--gray-s) 95% / .65) );--surface-glow: radial-gradient(circle at 30% 0%, hsl(var(--gray-h) var(--gray-s) 98% / .7), transparent 50%);--surface-border-glow: radial-gradient(at 50% 50%, hsl(var(--purple-h) 90% 90% / .8) 0, transparent 50%);--surface: var(--surface-glow) no-repeat border-box, var(--surface-background) no-repeat padding-box, hsl(var(--gray-h) var(--gray-s) 98% / .2);--surface-with-border-glow: var(--surface-glow) no-repeat border-box, linear-gradient(var(--background-color), var(--background-color)) no-repeat padding-box, var(--surface-border-glow) no-repeat border-box 0 0 / var(--glow-size, 600px) var(--glow-size, 600px);--surface-border-color: hsl(var(--gray-h) var(--gray-s) 100% / .7);--surface-backdrop-filter: blur(10px);--surface-box-shadow-1: 0 0 0 .5px hsl(var(--gray-h) var(--gray-s) 5% / .15), 0 6px 12px -1px hsl(var(--shadow-hsl) / .3);--surface-box-shadow-2: 0 0 0 .5px hsl(var(--gray-h) var(--gray-s) 5% / .15), 0 24px 40px -4px hsl(var(--shadow-hsl) / .4);--background-button: linear-gradient( hsl(var(--gray-h) var(--gray-s) 98% / .4), hsl(var(--gray-h) var(--gray-s) 90% / .2) );--background-button-active: hsl(var(--gray-h) var(--gray-s) 80% / .2);--color: var(--gray-500);--color-high-contrast: var(--gray-900);--color-accent: var(--purple-700);--color-danger: var(--red-700);--border-color: var(--gray-150);--border-color-high-contrast: var(--gray-300);--border-color-button: var(--gray-350);--border-color-popover: hsl(var(--gray-hsl) / .08);--border-color-dialog: hsl(var(--gray-hsl) / .08);--accent-color: var(--purple-600);--selection-color: hsl(var(--blue-hsl));--shadow-hsl: var(--gray-h) var(--gray-s) 20%;--lumo-contrast-5pct: var(--gray-100);--lumo-contrast-10pct: var(--gray-200);--lumo-contrast-60pct: var(--gray-400);--lumo-contrast-80pct: var(--gray-600);--lumo-contrast-90pct: var(--gray-800);--card-bg: rgba(255, 255, 255, .5);--card-hover-bg: rgba(255, 255, 255, .65);--card-open-bg: rgba(255, 255, 255, .8);--card-border: 1px solid rgba(0, 50, 100, .15);--card-open-shadow: 0px 1px 4px -1px rgba(28, 52, 84, .26);--card-section-border: var(--card-border);--card-field-bg: var(--lumo-contrast-5pct);--indicator-border: white}:host(.dark){color-scheme:dark;--surface-0: hsl(var(--gray-h) var(--gray-s) 10% / .85);--surface-1: hsl(var(--gray-h) var(--gray-s) 14% / .85);--surface-2: hsl(var(--gray-h) var(--gray-s) 18% / .85);--surface-background: linear-gradient( hsl(var(--gray-h) var(--gray-s) 8% / .65), hsl(var(--gray-h) var(--gray-s) 8% / .7) );--surface-glow: radial-gradient( circle at 30% 0%, hsl(var(--gray-h) calc(var(--gray-s) * 2) 90% / .12), transparent 50% );--surface: var(--surface-glow) no-repeat border-box, var(--surface-background) no-repeat padding-box, hsl(var(--gray-h) var(--gray-s) 20% / .4);--surface-border-glow: hsl(var(--gray-h) var(--gray-s) 20% / .4) radial-gradient(at 50% 50%, hsl(250 40% 80% / .4) 0, transparent 50%);--surface-border-color: hsl(var(--gray-h) var(--gray-s) 50% / .2);--surface-box-shadow-1: 0 0 0 .5px hsl(var(--purple-h) 40% 5% / .4), 0 6px 12px -1px hsl(var(--shadow-hsl) / .4);--surface-box-shadow-2: 0 0 0 .5px hsl(var(--purple-h) 40% 5% / .4), 0 24px 40px -4px hsl(var(--shadow-hsl) / .5);--color: var(--gray-650);--background-button: linear-gradient( hsl(var(--gray-h) calc(var(--gray-s) * 2) 80% / .1), hsl(var(--gray-h) calc(var(--gray-s) * 2) 80% / 0) );--background-button-active: hsl(var(--gray-h) var(--gray-s) 10% / .1);--border-color-popover: hsl(var(--gray-h) var(--gray-s) 90% / .1);--border-color-dialog: hsl(var(--gray-h) var(--gray-s) 90% / .1);--shadow-hsl: 0 0% 0%;--lumo-disabled-text-color: var(--lumo-contrast-60pct);--card-bg: rgba(255, 255, 255, .05);--card-hover-bg: rgba(255, 255, 255, .065);--card-open-bg: rgba(255, 255, 255, .1);--card-border: 1px solid rgba(255, 255, 255, .11);--card-open-shadow: 0px 1px 4px -1px rgba(0, 0, 0, .26);--card-section-border: var(--card-border);--card-field-bg: var(--lumo-contrast-10pct);--indicator-border: var(--lumo-base-color)}', Dc = "button{align-items:center;-webkit-appearance:none;appearance:none;background:transparent;background-origin:border-box;border:1px solid transparent;border-radius:var(--radius-1);color:var(--body-text-color);display:inline-flex;flex-shrink:0;font:var(--font-button);height:var(--size-m);justify-content:center;outline-offset:calc(var(--focus-size) / -1);padding:0 var(--space-100)}button:focus{animation-delay:0s,.15s;animation-duration:.15s,.45s;animation-name:button-focus-in,button-focus-out;animation-timing-function:cubic-bezier(.2,0,0,1),cubic-bezier(.2,0,0,1);outline:var(--focus-size) solid var(--focus-color)}button.icon{padding:0;width:var(--size-m)}button.icon span{display:contents}button.primary{background:var(--primary-color);color:var(--primary-contrast-text-color)}button .prefix,button .suffix{align-items:center;display:flex;height:var(--size-m);justify-content:center;width:var(--size-m)}button:has(.prefix){padding-inline-start:0}button:has(.suffix){padding-inline-end:0}button svg{height:var(--icon-size-s);width:var(--icon-size-s)}button:active:not([disabled]){background:var(--active-color)}button[disabled]{opacity:.5}button[hidden]{display:none}", Tc = ':is(vaadin-context-menu-overlay,vaadin-menu-bar-overlay,vaadin-select-overlay){z-index:var(--z-index-popover)}:is(vaadin-context-menu-overlay,vaadin-menu-bar-overlay,vaadin-select-overlay):first-of-type{padding-top:0}:is(vaadin-combo-box-overlay,vaadin-context-menu-overlay,vaadin-menu-bar-overlay,vaadin-popover-overlay,vaadin-select-overlay,vaadin-tooltip-overlay)::part(overlay){background:var(--background-color);-webkit-backdrop-filter:var(--surface-backdrop-filter);backdrop-filter:var(--surface-backdrop-filter);border-radius:var(--radius-1);box-shadow:var(--surface-box-shadow-1);margin-top:0}:is(vaadin-context-menu-overlay,vaadin-menu-bar-overlay,vaadin-select-overlay)::part(content){padding:var(--space-50)}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item){--_lumo-item-selected-icon-display: none;align-items:center;border-radius:var(--radius-1);color:var(--body-text-color);cursor:default;display:flex;font:var(--font-xsmall-medium);min-height:0;padding:calc((var(--size-m) - var(--line-height-1)) / 2) var(--space-100)}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item)[disabled],:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item)[disabled] .hint,:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item)[disabled] vaadin-icon{color:var(--lumo-disabled-text-color)}:is(vaadin-context-menu-item,vaadin-menu-bar-item):hover:not([disabled]),:is(vaadin-context-menu-item,vaadin-menu-bar-item)[expanded]:not([disabled]){background:var(--hover-color)}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item)[focus-ring]{outline:2px solid var(--selection-color);outline-offset:-2px}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item):is([aria-haspopup=true]):after{align-items:center;display:flex;height:var(--icon-size-m);justify-content:center;margin:0;padding:0;width:var(--icon-size-m)}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item).danger{color:var(--error-color);--color: currentColor}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item)::part(content){display:flex;align-items:center;gap:var(--space-100)}:is(vaadin-combo-box-item,vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) vaadin-icon{width:1em;height:1em;padding:0;color:var(--color)}:is(vaadin-context-menu-overlay,vaadin-menu-bar-overlay,vaadin-select-overlay) hr{margin:var(--space-50)}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item)>svg:first-child{color:var(--secondary-text-color)}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) .label{margin-inline-end:auto;padding-inline-end:var(--space-300)}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) .hint{color:var(--secondary-text-color)}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) kbd{align-items:center;display:inline-flex;border-radius:var(--radius-1);font:var(--font-xsmall);outline:1px solid var(--divider-primary-color);outline-offset:-1px;padding:0 var(--space-50)}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) .switch{align-items:center;border-radius:9999px;box-sizing:border-box;display:flex;height:.75rem;padding:var(--space-25);width:1.25rem}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) .switch.on{background:var(--switch-active-color);justify-content:end}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) .switch.off{background:var(--switch-inactive-color);justify-content:start}:is(vaadin-context-menu-item,vaadin-menu-bar-item,vaadin-select-item) .switch:before{background:#fff;border-radius:9999px;content:"";display:flex;height:.5rem;box-shadow:var(--shadow-m);width:.5rem}copilot-activation-button-user-info,copilot-activation-button-development-workflow{display:contents}:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .prefix,:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .suffix{align-items:center;display:flex;height:var(--icon-size-m);justify-content:center;width:var(--icon-size-m)}:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .content{display:flex;flex-direction:column;margin-inline-end:auto}:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .error{color:var(--error-color)}:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .warning{color:var(--warning-color)}:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .portrait{background-size:cover;border-radius:9999px;height:var(--icon-size-m);width:var(--icon-size-m)}:is(copilot-activation-button-user-info,copilot-activation-button-development-workflow) .dot{background-color:currentColor;border-radius:9999px;height:var(--space-75);width:var(--space-75)}vaadin-menu-bar-item[aria-selected=true]>svg:first-child{color:var(--blue-color)}:is(copilot-alignment-overlay)::part(content){padding:0}', kc = "code.codeblock{background:var(--contrast-color-5);border-radius:var(--radius-2);display:block;font-family:var(--monospace-font-family);font-size:var(--font-size-1);line-height:var(--line-height-1);overflow:hidden;padding:calc((var(--size-m) - var(--line-height-1)) / 2) var(--size-m) calc((var(--size-m) - var(--line-height-1)) / 2) var(--space-100);position:relative;text-overflow:ellipsis;white-space:pre;min-height:var(--line-height-1)}copilot-copy{position:absolute;right:0;top:0}", Ic = "vaadin-dialog-overlay::part(overlay){background:var(--background-color);-webkit-backdrop-filter:var(--surface-backdrop-filter);backdrop-filter:var(--surface-backdrop-filter);border:1px solid var(--contrast-color-5);border-radius:var(--radius-2);box-shadow:var(--surface-box-shadow-1)}vaadin-dialog-overlay::part(header){background:none;border-bottom:1px solid var(--divider-primary-color);box-sizing:border-box;font:var(--font-xsmall-semibold);min-height:var(--size-xl);padding:var(--space-50) var(--space-50) var(--space-50) var(--space-150)}vaadin-dialog-overlay h2{font:var(--font-xsmall-bold);margin:0;padding:0}vaadin-dialog-overlay::part(content){font:var(--font-xsmall);padding:var(--space-150)}vaadin-dialog-overlay::part(footer){background:none;padding:var(--space-100)}vaadin-dialog-overlay.ai-dialog::part(overlay){max-width:20rem}vaadin-dialog-overlay.ai-dialog::part(header){border:none}vaadin-dialog-overlay.ai-dialog [slot=header-content] svg{color:var(--blue-color)}vaadin-dialog-overlay.ai-dialog::part(content){display:flex;flex-direction:column;gap:var(--space-200)}vaadin-dialog-overlay.ai-dialog p{margin:0}vaadin-dialog-overlay.ai-dialog label:has(input[type=checkbox]){align-items:center;display:flex}vaadin-dialog-overlay.ai-dialog input[type=checkbox]{height:.875rem;margin:calc((var(--size-m) - .875rem) / 2);width:.875rem}vaadin-dialog-overlay.ai-dialog button.primary{min-width:calc(var(--size-m) * 2)}vaadin-dialog-overlay.custom-component-api-dialog-overlay::part(overlay){width:25em}vaadin-dialog-overlay.custom-component-api-dialog-overlay::part(header-content){width:unset;justify-content:unset;flex:unset}vaadin-dialog-overlay.custom-component-api-dialog-overlay::part(title){font-size:var(--font-size-2)}vaadin-dialog-overlay.custom-component-api-dialog-overlay::part(header){border-bottom:unset;justify-content:space-between}vaadin-dialog-overlay.custom-component-api-dialog-overlay::part(content){padding:var(--space-100);max-height:250px;overflow:auto}vaadin-dialog-overlay.custom-component-api-dialog-overlay div.item-content{display:flex;justify-content:center;align-items:start;flex-direction:column}vaadin-dialog-overlay.edit-component-dialog-overlay{width:25em}vaadin-dialog-overlay.edit-component-dialog-overlay #component-icon{width:75px}", Vc = "vaadin-popover-overlay::part(overlay){background:var(--surface);font:var(--font-xsmall)}vaadin-popover-overlay{--vaadin-button-font-size: var(--font-size-1);--vaadin-button-height: var(--line-height-4)}", Rc = ":host{--vaadin-input-field-label-font-size: var(--font-size-1);--vaadin-select-label-font-size: var(--font-size-1);--vaadin-button-font-size: var(--font-size-2);--vaadin-checkbox-label-font-size: var(--font-size-1);--vaadin-input-field-value-font-size: var(--font-xsmall);--vaadin-input-field-background: transparent;--vaadin-input-field-border-color: var(--input-border-color);--vaadin-input-field-border-radius: var(--radius-1);--vaadin-input-field-border-width: 1px;--vaadin-input-field-height: var(--size-m);--vaadin-input-field-helper-font-size: var(--font-size-1);--vaadin-input-field-helper-spacing: var(--space-50)}";
var Tu = typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : typeof global < "u" ? global : typeof self < "u" ? self : {};
function jc(e) {
  return e && e.__esModule && Object.prototype.hasOwnProperty.call(e, "default") ? e.default : e;
}
function ku(e) {
  if (Object.prototype.hasOwnProperty.call(e, "__esModule")) return e;
  var t = e.default;
  if (typeof t == "function") {
    var n = function r() {
      return this instanceof r ? Reflect.construct(t, arguments, this.constructor) : t.apply(this, arguments);
    };
    n.prototype = t.prototype;
  } else n = {};
  return Object.defineProperty(n, "__esModule", { value: !0 }), Object.keys(e).forEach(function(r) {
    var i = Object.getOwnPropertyDescriptor(e, r);
    Object.defineProperty(n, r, i.get ? i : {
      enumerable: !0,
      get: function() {
        return e[r];
      }
    });
  }), n;
}
var Vt = { exports: {} }, Yr;
function Mc() {
  if (Yr) return Vt.exports;
  Yr = 1;
  function e(t, n = 100, r = {}) {
    if (typeof t != "function")
      throw new TypeError(`Expected the first parameter to be a function, got \`${typeof t}\`.`);
    if (n < 0)
      throw new RangeError("`wait` must not be negative.");
    const { immediate: i } = typeof r == "boolean" ? { immediate: r } : r;
    let o, a, s, l, c;
    function u() {
      const p = o, b = a;
      return o = void 0, a = void 0, c = t.apply(p, b), c;
    }
    function d() {
      const p = Date.now() - l;
      p < n && p >= 0 ? s = setTimeout(d, n - p) : (s = void 0, i || (c = u()));
    }
    const v = function(...p) {
      if (o && this !== o && Object.getPrototypeOf(this) === Object.getPrototypeOf(o))
        throw new Error("Debounced method called with different contexts of the same prototype.");
      o = this, a = p, l = Date.now();
      const b = i && !s;
      return s || (s = setTimeout(d, n)), b && (c = u()), c;
    };
    return Object.defineProperty(v, "isPending", {
      get() {
        return s !== void 0;
      }
    }), v.clear = () => {
      s && (clearTimeout(s), s = void 0);
    }, v.flush = () => {
      s && v.trigger();
    }, v.trigger = () => {
      c = u(), v.clear();
    }, v;
  }
  return Vt.exports.debounce = e, Vt.exports = e, Vt.exports;
}
var Lc = /* @__PURE__ */ Mc();
const zc = /* @__PURE__ */ jc(Lc);
class Uc {
  constructor() {
    this.documentActive = !0, this.addListeners = () => {
      window.addEventListener("pageshow", this.handleWindowVisibilityChange), window.addEventListener("pagehide", this.handleWindowVisibilityChange), window.addEventListener("focus", this.handleWindowFocusChange), window.addEventListener("blur", this.handleWindowFocusChange), document.addEventListener("visibilitychange", this.handleDocumentVisibilityChange);
    }, this.removeListeners = () => {
      window.removeEventListener("pageshow", this.handleWindowVisibilityChange), window.removeEventListener("pagehide", this.handleWindowVisibilityChange), window.removeEventListener("focus", this.handleWindowFocusChange), window.removeEventListener("blur", this.handleWindowFocusChange), document.removeEventListener("visibilitychange", this.handleDocumentVisibilityChange);
    }, this.handleWindowVisibilityChange = (t) => {
      t.type === "pageshow" ? this.dispatch(!0) : this.dispatch(!1);
    }, this.handleWindowFocusChange = (t) => {
      t.type === "focus" ? this.dispatch(!0) : this.dispatch(!1);
    }, this.handleDocumentVisibilityChange = () => {
      this.dispatch(!document.hidden);
    }, this.dispatch = (t) => {
      if (t !== this.documentActive) {
        const n = window.Vaadin.copilot.eventbus;
        this.documentActive = t, n.emit("document-activation-change", { active: this.documentActive });
      }
    };
  }
  copilotActivated() {
    this.addListeners();
  }
  copilotDeactivated() {
    this.removeListeners();
  }
}
const Zr = new Uc(), Bc = "copilot-development-setup-user-guide";
function Iu() {
  _t("use-dev-workflow-guide"), se.updatePanel(Bc, { floating: !0 });
}
function Io() {
  const e = g.jdkInfo;
  return e ? e.jrebel ? "success" : e.hotswapAgentFound ? !e.hotswapVersionOk || !e.runningWithExtendClassDef || !e.runningWitHotswap || !e.runningInJavaDebugMode ? "error" : "success" : "warning" : null;
}
function Vu() {
  const e = g.jdkInfo;
  return !e || Io() !== "success" ? "none" : e.jrebel ? "jrebel" : e.runningWitHotswap ? "hotswap" : "none";
}
function Fc() {
  return g.idePluginState?.ide === "eclipse" ? "unsupported" : g.idePluginState !== void 0 && !g.idePluginState.active ? "warning" : "success";
}
function Ru() {
  if (!g.jdkInfo)
    return { status: "success" };
  const e = Io(), t = Fc();
  return e === "warning" ? t === "warning" ? { status: "warning", message: "IDE Plugin, Hotswap" } : { status: "warning", message: "Hotswap is not enabled" } : t === "warning" ? { status: "warning", message: "IDE Plugin is not active" } : e === "error" ? { status: "error", message: "Hotswap is partially enabled" } : { status: "success" };
}
function Hc() {
  te(`${ve}get-dev-setup-info`, {}), window.Vaadin.copilot.eventbus.on("copilot-get-dev-setup-info-response", (e) => {
    if (e.detail.content) {
      const t = JSON.parse(e.detail.content);
      g.setIdePluginState(t.ideInfo), g.setJdkInfo(t.jdkInfo);
    }
  });
}
const st = /* @__PURE__ */ new WeakMap();
class Kc {
  constructor() {
    this.root = null, this.nodeUuidNodeMapFlat = /* @__PURE__ */ new Map(), this._hasFlowComponent = !1, this.flowNodesInSource = {}, this.flowCustomComponentData = {};
  }
  async init() {
    const t = Ks();
    t && await this.addToTree(t) && (await this.addOverlayContentToTreeIfExists("vaadin-popover-overlay"), await this.addOverlayContentToTreeIfExists("vaadin-dialog-overlay"));
  }
  getChildren(t) {
    return this.nodeUuidNodeMapFlat.get(t)?.children ?? [];
  }
  get allNodesFlat() {
    return Array.from(this.nodeUuidNodeMapFlat.values());
  }
  getNodeOfElement(t) {
    if (t)
      return this.allNodesFlat.find((n) => n.element === t);
  }
  /**
   * Handles route containers that should not be present in the tree. When this returns <code>true</code>, it means that given node is a route container so adding it to tree should be skipped
   *
   * @param node Node to check whether it is a route container or not
   * @param parentNode Parent of the given {@link node}
   */
  async handleRouteContainers(t, n) {
    const r = Nr(t);
    if (!r && Zs(t)) {
      const i = Qt(t);
      if (i && i.nextElementSibling)
        return await this.addToTree(i.nextElementSibling, n), !0;
    }
    if (r && t.localName === "react-router-outlet") {
      for (const i of Array.from(t.children)) {
        const o = Zt(i);
        o && await this.addToTree(o, n);
      }
      return !0;
    }
    return !1;
  }
  includeReactNode(t) {
    return ct(t) === "PreconfiguredAuthProvider" || ct(t) === "RouterProvider" ? !1 : xr(t) || Js(t);
  }
  async includeFlowNode(t) {
    return Qs(t) || En(t)?.hiddenByServer ? !1 : this.isInitializedInProjectSources(t);
  }
  async isInitializedInProjectSources(t) {
    const n = En(t);
    if (!n)
      return !1;
    const { nodeId: r, uiId: i } = n;
    if (!this.flowNodesInSource[i]) {
      const o = await hn("copilot-get-component-source-info", { uiId: i }, (a) => a.data);
      o.error && le("Failed to get component source info", o.error), this.flowCustomComponentData[i] = o.customComponentResponse, this.flowNodesInSource[i] = new Set(o.nodeIdsInProject);
    }
    return this.flowNodesInSource[i].has(r);
  }
  /**
   * Adds the given element into the tree and returns the result when added.
   * <p>
   *  It recursively travels through the children of given node. This method is called for each child ,but the result of adding a child is swallowed
   * </p>
   * @param node Node to add to tree
   * @param parentNode Parent of the node, might be null if it is the root element
   */
  async addToTree(t, n) {
    const r = await this.handleRouteContainers(t, n);
    if (r)
      return r;
    const i = Nr(t);
    let o;
    if (!i)
      this.includeReactNode(t) && (o = this.generateNodeFromFiber(t, n));
    else if (await this.includeFlowNode(t)) {
      const l = this.generateNodeFromFlow(t, n);
      if (!l)
        return !1;
      this._hasFlowComponent = !0, o = l;
    }
    if (n)
      o && (o.parent = n, n.children || (n.children = []), n.children.push(o));
    else {
      if (!o)
        return !(t instanceof Element) && Zi(t) ? (Eo({
          type: Ve.WARNING,
          message: "Copilot is partly usable",
          details: `${ct(t)} should be a function component to make Copilot work properly`,
          dismissId: "react_route_component_is_class"
        }), !1) : (le("Unable to add node", new Error("Tree root node is undefined")), !1);
      this.root = o;
    }
    o && this.nodeUuidNodeMapFlat.set(o.uuid, o);
    const a = o ?? n, s = i ? Array.from(t.children) : qs(t);
    for (const l of s)
      await this.addToTree(l, a);
    return o !== void 0;
  }
  generateNodeFromFiber(t, n) {
    const r = xr(t) ? Qt(t) : void 0, i = n?.children.length ?? 0;
    return {
      node: t,
      parent: n,
      element: r,
      depth: n && n.depth + 1 || 0,
      children: [],
      siblingIndex: i,
      isFlowComponent: !1,
      isReactComponent: !0,
      get uuid() {
        if (st.has(t))
          return st.get(t);
        if (t.alternate && st.has(t.alternate))
          return st.get(t.alternate);
        const a = Do();
        return st.set(t, a), a;
      },
      get name() {
        return Cr(ct(t));
      },
      get identifier() {
        return Pr(r);
      },
      get nameAndIdentifier() {
        return ei(this.name, this.identifier);
      },
      get previousSibling() {
        if (i !== 0)
          return n?.children[i - 1];
      },
      get nextSibling() {
        if (!(n === void 0 || i === n.children.length - 1))
          return n.children[i + 1];
      },
      get path() {
        return Qr(this);
      }
    };
  }
  generateNodeFromFlow(t, n) {
    const r = En(t);
    if (!r)
      return;
    const i = n?.children.length ?? 0, o = this.flowCustomComponentData;
    return {
      node: r,
      parent: n,
      element: t,
      depth: n && n.depth + 1 || 0,
      children: [],
      siblingIndex: i,
      get uuid() {
        return `${r.uiId}#${r.nodeId}`;
      },
      isFlowComponent: !0,
      isReactComponent: !1,
      get name() {
        return Ys(r) ?? Cr(r.element.localName);
      },
      get identifier() {
        return Pr(t);
      },
      get nameAndIdentifier() {
        return ei(this.name, this.identifier);
      },
      get previousSibling() {
        if (i !== 0)
          return n?.children[i - 1];
      },
      get nextSibling() {
        if (!(n === void 0 || i === n.children.length - 1))
          return n.children[i + 1];
      },
      get path() {
        return Qr(this);
      },
      get customComponentData() {
        if (o[r.uiId])
          return o[r.uiId].allComponentsInfoForCustomComponentSupport[r.nodeId];
      }
    };
  }
  async addOverlayContentToTreeIfExists(t) {
    const n = document.body.querySelector(t);
    if (!n)
      return;
    const r = n.owner;
    if (!r)
      return;
    let i = !0;
    if (!this.getNodeOfElement(r)) {
      const o = $e(Zt(r));
      i = await this.addToTree(o ?? r, this.root);
    }
    if (i)
      for (const o of Array.from(n.children))
        await this.addToTree(o, this.getNodeOfElement(r));
  }
  hasFlowComponents() {
    return this._hasFlowComponent;
  }
  findNodeByUuid(t) {
    if (t)
      return this.nodeUuidNodeMapFlat.get(t);
  }
  getElementByNodeUuid(t) {
    return this.findNodeByUuid(t)?.element;
  }
  findByTreePath(t) {
    if (t)
      return this.allNodesFlat.find((n) => n.path === t);
  }
}
function Qr(e) {
  if (!e.parent)
    return e.name;
  let t = 0;
  for (let n = 0; n < e.siblingIndex + 1; n++)
    e.parent.children[n].name === e.name && t++;
  return `${e.parent.path} > ${e.name}[${t}]`;
}
function ei(e, t) {
  return t ? `${e} "${t}"` : e;
}
const qc = async () => {
  const e = new Kc();
  await e.init(), window.Vaadin.copilot.tree.currentTree = e;
};
function Wc() {
  const e = window.navigator.userAgent;
  return e.indexOf("Windows") !== -1 ? "Windows" : e.indexOf("Mac") !== -1 ? "Mac" : e.indexOf("Linux") !== -1 ? "Linux" : null;
}
function Gc() {
  return Wc() === "Mac";
}
function Jc() {
  return Gc() ? "⌘" : "Ctrl";
}
let Cn = !1, lt = 0;
const Hn = (e) => {
  if (Ct.isActivationShortcut())
    if (e.key === "Shift" && !e.ctrlKey && !e.altKey && !e.metaKey)
      Cn = !0;
    else if (Cn && e.shiftKey && (e.key === "Control" || e.key === "Meta")) {
      if (lt++, lt === 2)
        return g.toggleActive("shortcut"), lt = 0, !0;
      setTimeout(() => {
        lt = 0;
      }, 500);
    } else
      Cn = !1, lt = 0;
  return !1;
};
function ti(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === "c") {
    const n = document.querySelector("copilot-main")?.shadowRoot?.getSelection();
    if (n && n.rangeCount === 1) {
      const i = n.getRangeAt(0).commonAncestorContainer;
      return qe(i);
    }
  }
  return !1;
}
function Xc(e) {
  const t = Ln(e, "vaadin-context-menu-overlay");
  if (!t)
    return !1;
  const n = t.owner;
  return n ? !!Ln(n, "copilot-component-overlay") : !1;
}
function Yc() {
  return g.idePluginState?.supportedActions?.find((e) => e === "undo");
}
const ni = (e) => {
  if (!g.active)
    return;
  if (Hn(e)) {
    e.stopPropagation();
    return;
  }
  const t = ll();
  if (!t)
    return;
  const n = Xc(t);
  if (!(t.localName === "copilot-main") && !n) {
    e.stopPropagation();
    return;
  }
  let i = !0, o = !1;
  ti(e) ? i = !1 : e.key === "Escape" ? g.loginCheckActive ? g.setLoginCheckActive(!1) : y.emit("close-drawers", {}) : Zc(e) ? (y.emit("delete-selected", {}), o = !0) : (e.ctrlKey || e.metaKey) && e.key === "d" ? (y.emit("duplicate-selected", {}), o = !0) : (e.ctrlKey || e.metaKey) && e.key === "b" ? (y.emit("show-selected-in-ide", { attach: e.shiftKey }), o = !0) : (e.ctrlKey || e.metaKey) && e.key === "z" && Yc() ? (y.emit("undoRedo", { undo: !e.shiftKey }), o = !0) : ti(e) || y.emit("keyboard-event", { event: e }), i && e.stopPropagation(), o && e.preventDefault();
}, Zc = (e) => (e.key === "Backspace" || e.key === "Delete") && !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey, ne = Jc(), Rt = "⇧", ju = {
  toggleCopilot: `<kbd>${Rt}</kbd> + <kbd>${ne}</kbd> <kbd>${ne}</kbd>`,
  toggleCommandWindow: `<kbd>${Rt}</kbd> + <kbd>Space</kbd>`,
  undo: `<kbd>${ne}</kbd> + <kbd>Z</kbd>`,
  redo: `<kbd>${ne}</kbd> + <kbd>${Rt}</kbd> + <kbd>Z</kbd>`,
  duplicate: `<kbd>${ne}</kbd> + <kbd>D</kbd>`,
  goToSource: `<kbd>${ne}</kbd> + <kbd>B</kbd>`,
  goToAttachSource: `<kbd>${ne}</kbd>  + <kbd>${Rt}</kbd>  + <kbd>B</kbd>`,
  selectParent: "<kbd>←</kbd>",
  selectPreviousSibling: "<kbd>↑</kbd>",
  selectNextSibling: "<kbd>↓</kbd>",
  delete: "<kbd>DEL</kbd>",
  copy: `<kbd>${ne}</kbd> + <kbd>C</kbd>`,
  paste: `<kbd>${ne}</kbd> + <kbd>V</kbd>`
};
var Qc = Object.getOwnPropertyDescriptor, eu = (e, t, n, r) => {
  for (var i = r > 1 ? void 0 : r ? Qc(t, n) : t, o = e.length - 1, a; o >= 0; o--)
    (a = e[o]) && (i = a(i) || i);
  return i;
};
let ri = class extends Kl {
  constructor() {
    super(...arguments), this.removers = [], this.initialized = !1, this.active = !1, this.toggleOperationInProgressAttr = () => {
      this.toggleAttribute("operation-in-progress", g.operationWaitsHmrUpdate !== void 0);
    }, this.operationInProgressCursorUpdateDebounceFunc = zc(this.toggleOperationInProgressAttr, 500), this.overlayOutsideClickListener = (e) => {
      qe(e.target?.owner) || (g.active || qe(e.detail.sourceEvent.target)) && e.preventDefault();
    }, this.mouseOverListener = () => {
      g.activatedFrom !== "test" && (g.pointerEventsDisabledForScrolling || y.emit("set-pointer-events", { enable: !0 }));
    }, this.mouseLeaveListener = () => {
      g.activatedFrom !== "test" && y.emit("set-pointer-events", { enable: !1 }), y.emit("close-drawers", {});
    };
  }
  static get styles() {
    return [
      J(Cc),
      J(Dc),
      J(kc),
      J(Pc),
      J(Rc),
      J(Ic),
      J(Vc),
      J(Tc),
      J($c),
      yl`
        :host {
          color: var(--body-text-color);
          contain: strict;
          cursor: var(--cursor, default);
          font: var(--font-xsmall);
          inset: 0;
          pointer-events: all;
          position: fixed;
          z-index: 9999;
        }

        :host([operation-in-progress]) {
          --cursor: wait;
          --lumo-clickable-cursor: wait;
        }

        :host(:not([active])) {
          visibility: hidden !important;
          pointer-events: none;
        }

        /* Hide floating panels when not active */

        :host(:not([active])) > copilot-section-panel-wrapper {
          display: none !important;
        }
        :host(:not([active])) > copilot-section-panel-wrapper[individual] {
          display: block !important;
          visibility: visible;
          pointer-events: all;
        }

        /* Keep activation button and menu visible */

        copilot-activation-button,
        .activation-button-menu {
          visibility: visible;
          display: flex !important;
        }

        copilot-activation-button {
          pointer-events: auto;
        }

        a {
          border-radius: var(--radius-2);
          color: var(--blue-color);
          outline-offset: calc(var(--focus-size) / -1);
        }

        a:focus {
          animation-delay: 0s, 0.15s;
          animation-duration: 0.15s, 0.45s;
          animation-name: link-focus-in, link-focus-out;
          animation-timing-function: cubic-bezier(0.2, 0, 0, 1), cubic-bezier(0.2, 0, 0, 1);
          outline: var(--focus-size) solid var(--blue-color);
        }

        :host([user-select-none]) {
          -webkit-touch-callout: none;
          -webkit-user-select: none;
          -moz-user-select: none;
          -ms-user-select: none;
          user-select: none;
        }

        /* Needed to prevent a JS error because of monkey patched '_attachOverlay'. It is some scope issue, */
        /* where 'this._placeholder.parentNode' is undefined - the scope if 'this' gets messed up at some point. */
        /* We also don't want animations on the overlays to make the feel faster, so this is fine. */

        :is(
            vaadin-context-menu-overlay,
            vaadin-menu-bar-overlay,
            vaadin-select-overlay,
            vaadin-combo-box-overlay,
            vaadin-tooltip-overlay
          ):is([opening], [closing]),
        :is(
            vaadin-context-menu-overlay,
            vaadin-menu-bar-overlay,
            vaadin-select-overlay,
            vaadin-combo-box-overlay,
            vaadin-tooltip-overlay
          )::part(overlay) {
          animation: none !important;
        }

        :host(:not([active])) copilot-drawer-panel::before {
          animation: none;
        }

        /* Workaround for https://github.com/vaadin/web-components/issues/5400 */

        :host(:not([active])) .activation-button-menu .toggle-spotlight {
          display: none;
        }
      `
    ];
  }
  connectedCallback() {
    super.connectedCallback(), this.init().catch((e) => le("Unable to initialize copilot", e));
  }
  async init() {
    if (this.initialized)
      return;
    await window.Vaadin.copilot._machineState.initializer.promise, await import("./copilot-global-vars-later-BLROO9Hi.js"), await import("./copilot-init-step2-BhnYm4bs.js"), rl(), ql(), this.tabIndex = 0, It.hostConnectedCallback(), window.addEventListener("keydown", Hn), this.addEventListener("keydown", ni), y.onSend(this.handleSendEvent), this.removers.push(y.on("close-drawers", this.closeDrawers.bind(this))), this.removers.push(
      y.on("open-attention-required-drawer", this.openDrawerIfPanelRequiresAttention.bind(this))
    ), this.removers.push(
      y.on("set-pointer-events", (t) => {
        this.style.pointerEvents = t.detail.enable ? "" : "none";
      })
    ), this.addEventListener("mousemove", this.mouseMoveListener), this.addEventListener("dragover", this.mouseMoveListener), ot.addOverlayOutsideClickEvent();
    const e = window.matchMedia("(prefers-color-scheme: dark)");
    this.classList.toggle("dark", e.matches), e.addEventListener("change", (t) => {
      this.classList.toggle("dark", e.matches);
    }), this.reaction(
      () => g.spotlightActive,
      () => {
        fe.saveSpotlightActivation(g.spotlightActive), Array.from(this.shadowRoot.querySelectorAll("copilot-section-panel-wrapper")).filter((t) => t.panelInfo?.floating === !0).forEach((t) => {
          g.spotlightActive ? t.style.setProperty("display", "none") : t.style.removeProperty("display");
        });
      }
    ), this.reaction(
      () => g.active,
      () => {
        this.toggleAttribute("active", g.active), g.active ? this.activate() : this.deactivate(), fe.saveCopilotActivation(g.active);
      }
    ), this.reaction(
      () => g.activatedAtLeastOnce,
      () => {
        No(), Wl();
      }
    ), this.reaction(
      () => g.sectionPanelDragging,
      () => {
        g.sectionPanelDragging && Array.from(this.shadowRoot.children).filter((n) => n.localName.endsWith("-overlay")).forEach((n) => {
          n.close && n.close();
        });
      }
    ), this.reaction(
      () => g.operationWaitsHmrUpdate,
      () => {
        g.operationWaitsHmrUpdate ? this.operationInProgressCursorUpdateDebounceFunc() : (this.operationInProgressCursorUpdateDebounceFunc.clear(), this.toggleOperationInProgressAttr());
      }
    ), this.reaction(
      () => se.panels,
      () => {
        se.panels.find((t) => t.individual) && this.requestUpdate();
      }
    ), fe.getCopilotActivation() && no().then(() => {
      g.setActive(!0, "restore");
    }), this.removers.push(
      y.on("user-select", (t) => {
        const { allowSelection: n } = t.detail;
        this.toggleAttribute("user-select-none", !n);
      })
    ), this.removers.push(
      y.on("featureFlags", (t) => {
        const n = t.detail.features;
        g.setFeatureFlags(n);
      })
    ), So(), this.initialized = !0, Hc();
  }
  /**
   * Called when Copilot is activated. Good place to start attach listeners etc.
   */
  async activate() {
    It.activate(), Zr.copilotActivated(), Gl(), this.openDrawerIfPanelRequiresAttention(), document.documentElement.addEventListener("mouseleave", this.mouseLeaveListener), document.documentElement.addEventListener("mouseover", this.mouseOverListener), ot.onCopilotActivation(), await qc(), Oo.loadPreviewConfiguration(), this.active = !0;
  }
  /**
   * Called when Copilot is deactivated. Good place to remove listeners etc.
   */
  deactivate() {
    this.closeDrawers(), It.deactivate(), Zr.copilotDeactivated(), document.documentElement.removeEventListener("mouseleave", this.mouseLeaveListener), document.documentElement.removeEventListener("mouseover", this.mouseOverListener), ot.onCopilotDeactivation(), this.active = !1;
  }
  disconnectedCallback() {
    super.disconnectedCallback(), It.hostDisconnectedCallback(), window.removeEventListener("keydown", Hn), this.removeEventListener("keydown", ni), y.offSend(this.handleSendEvent), this.removers.forEach((e) => e()), this.removeEventListener("mousemove", this.mouseMoveListener), this.removeEventListener("dragover", this.mouseMoveListener), ot.removeOverlayOutsideClickEvent(), document.documentElement.removeEventListener("vaadin-overlay-outside-click", this.overlayOutsideClickListener);
  }
  handleSendEvent(e) {
    const t = e.detail.command, n = e.detail.data;
    te(t, n);
  }
  /**
   * Opens the attention required drawer if there is any.
   */
  openDrawerIfPanelRequiresAttention() {
    const e = se.getAttentionRequiredPanelConfiguration();
    if (!e)
      return;
    const t = e.panel;
    if (!t || e.floating)
      return;
    const n = this.shadowRoot.querySelector(`copilot-drawer-panel[position="${t}"]`);
    n.opened = !0;
  }
  render() {
    return Ue`
      <copilot-activation-button
        @activation-btn-clicked="${() => {
      g.toggleActive("button"), g.setLoginCheckActive(!1);
    }}"
        @spotlight-activation-changed="${(e) => {
      g.setSpotlightActive(e.detail);
    }}"
        .spotlightOn="${g.spotlightActive}">
      </copilot-activation-button>
      <copilot-component-selector></copilot-component-selector>
      <copilot-label-editor-container></copilot-label-editor-container>
      <copilot-info-tooltip></copilot-info-tooltip>
      ${this.renderDrawer("left")} ${this.renderDrawer("right")} ${this.renderDrawer("bottom")} ${wc()}
      ${this.renderSpotlight()}
      <copilot-login-check ?active=${g.loginCheckActive && g.active}></copilot-login-check>
      <copilot-ai-usage-confirmation-dialog></copilot-ai-usage-confirmation-dialog>
      <copilot-notifications-container></copilot-notifications-container>
    `;
  }
  renderSpotlight() {
    return Ue`
      <copilot-spotlight ?active=${g.spotlightActive && g.active}></copilot-spotlight>
    `;
  }
  renderDrawer(e) {
    return Ue` <copilot-drawer-panel no-transition position=${e}>
      ${yc(e)}
    </copilot-drawer-panel>`;
  }
  /**
   * Closes the open drawers if any opened unless an overlay is opened from drawer.
   */
  closeDrawers() {
    const e = this.shadowRoot.querySelectorAll(`${ve}drawer-panel`);
    if (!Array.from(e).some((o) => o.opened))
      return;
    const n = Array.from(this.shadowRoot.children).find(
      (o) => o.localName.endsWith("overlay")
    ), r = n && ot.getOwner(n);
    if (!r) {
      e.forEach((o) => {
        o.opened = !1;
      });
      return;
    }
    const i = Ln(r, "copilot-drawer-panel");
    if (!i) {
      e.forEach((o) => {
        o.opened = !1;
      });
      return;
    }
    Array.from(e).filter((o) => o.position !== i.position).forEach((o) => {
      o.opened = !1;
    });
  }
  updated(e) {
    super.updated(e), this.attachActivationButtonToBody(), Nc();
  }
  attachActivationButtonToBody() {
    const e = document.body.querySelectorAll("copilot-activation-button");
    e.length > 1 && e[0].remove();
  }
  mouseMoveListener(e) {
    e.composedPath().find((t) => t.localName === `${ve}drawer-panel`) || this.closeDrawers();
  }
};
ri = eu([
  _l("copilot-main")
], ri);
const tu = window.Vaadin, nu = {
  init(e) {
    Qi(
      () => window.Vaadin.devTools,
      (t) => {
        const n = t.handleFrontendMessage;
        t.handleFrontendMessage = (r) => {
          xc(r) || n.call(t, r);
        };
      }
    );
  }
};
tu.devToolsPlugins.push(nu);
export {
  Dc as $,
  yl as A,
  ou as B,
  fe as C,
  Ct as D,
  O as E,
  Ue as F,
  ht as G,
  Su as H,
  Bc as I,
  Io as J,
  ru as K,
  Vu as L,
  Ve as M,
  au as N,
  xu as O,
  ve as P,
  Ru as Q,
  Oo as R,
  mu as S,
  Eo as T,
  zc as U,
  su as V,
  Iu as W,
  ju as X,
  nl as Y,
  hu as Z,
  Pu as _,
  jc as a,
  $u as a0,
  Xl as a1,
  iu as a2,
  cu as a3,
  _t as a4,
  kc as a5,
  fu as a6,
  yo as a7,
  qc as a8,
  _o as a9,
  Cu as aa,
  mo as ab,
  Bl as ac,
  bo as ad,
  du as ae,
  xo as af,
  Fn as ag,
  el as ah,
  Nu as ai,
  Fc as aj,
  No as ak,
  uo as al,
  Un as am,
  Eu as an,
  y as b,
  Tu as c,
  Yt as d,
  Us as e,
  uu as f,
  ku as g,
  vu as h,
  lu as i,
  g as j,
  hn as k,
  le as l,
  Xe as m,
  bu as n,
  S as o,
  vn as p,
  Du as q,
  Kc as r,
  te as s,
  pu as t,
  _l as u,
  Kl as v,
  se as w,
  gu as x,
  J as y,
  Cc as z
};
