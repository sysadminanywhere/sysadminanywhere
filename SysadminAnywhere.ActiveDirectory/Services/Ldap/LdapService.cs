using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;

namespace SysadminAnywhere.ActiveDirectory.Services.Ldap
{
    public class LdapService : ILdapService, IDisposable
    {

        LdapConnection? connection = null;

        public string DefaultNamingContext { get; set; }
        public string DomainName { get; set; }

        public LdapService(string host, int port, string userName, string password, bool isSSL = true)
        {

            /*
             You can install and configure the Active Directory Certificate Services (AD CS) role on a domain controller.
             */

            try
            {
                if (isSSL)
                {
                    LdapConnectionOptions options = new LdapConnectionOptions()
                        .ConfigureRemoteCertificateValidationCallback(new System.Net.Security.RemoteCertificateValidationCallback((a, b, c, d) => true))
                        .UseSsl();

                    connection = new LdapConnection(options);
                }
                else {
                    connection = new LdapConnection();
                }

                connection.Connect(host, port);

                ILdapSearchResults searchResults = connection.Search(DefaultNamingContext, LdapConnection.ScopeBase, "(objectclass=*)", null, false);

                while (searchResults.HasMore())
                {
                    var nextEntry = searchResults.Next();
                    DefaultNamingContext = nextEntry.GetAttribute("defaultNamingContext").StringValue;
                }

                DomainName = DefaultNamingContext.ToUpper().Replace("DC=", "").Replace(",", ".").ToLower();
            }
            catch (Exception ex)
            {
                connection = null;
            }
        }

        public bool IsConnected()
        {
            if (connection == null) return false;
            else return connection.Connected;
        }

        public List<LdapEntry> Search(string filter)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(filter))
                throw new ArgumentNullException(nameof(filter));

            return Search(DefaultNamingContext, filter);
        }

        public List<LdapEntry> Search(string path, string filter, int scope = LdapConnection.ScopeSub)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(path))
                throw new ArgumentNullException(nameof(path));

            if (string.IsNullOrEmpty(filter))
                throw new ArgumentNullException(nameof(filter));

            List<LdapEntry> results = new List<LdapEntry>();

            ILdapSearchResults searchResults = connection.Search(DefaultNamingContext, scope, filter, null, false);

            while (searchResults.HasMore())
            {
                var nextEntry = searchResults.Next();
                results.Add(nextEntry);
            }

            return results;
        }

        public void SendRequest(string dn, List<LdapModification> ldapModifications)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (ldapModifications == null)
                throw new ArgumentNullException(nameof(ldapModifications));

            connection.Modify(dn, ldapModifications.ToArray());
        }

        public void SendRequest(string dn, LdapModification ldapModification)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (ldapModification == null)
                throw new ArgumentNullException(nameof(ldapModification));

            connection.Modify(dn, ldapModification);
        }

        public void Add(LdapEntry entry)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (entry == null)
                throw new ArgumentNullException(nameof(entry));

            foreach (var item in from KeyValuePair<string, LdapAttribute> item in entry.GetAttributeSet().ToList()
                                 where string.IsNullOrEmpty(item.Value.StringValue)
                                 select item)
            {
                entry.GetAttributeSet().Remove(item.Key);
            }

            connection.Add(entry);
        }

        public void Modify(string dn, LdapModification entry)
        {
            if (connection == null)
                throw new NullReferenceException(nameof(connection));

            if (entry == null)
                throw new ArgumentNullException(nameof(entry));

            connection.Modify(dn, entry);
        }

        public void ModifyProperty(string dn, string name, string value)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(dn))
                throw new ArgumentNullException(nameof(dn));

            if (string.IsNullOrEmpty(name))
                throw new ArgumentNullException(nameof(name));

            if (string.IsNullOrEmpty(value))
                throw new ArgumentNullException(nameof(value));

            LdapAttribute attribute = new LdapAttribute(name, value);
            LdapModification ldapModification = new LdapModification(LdapModification.Replace, attribute);

            connection.Modify(dn, ldapModification);
        }

        public void Delete(string dn)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(dn))
                throw new ArgumentNullException(nameof(dn));

            connection.Delete(dn);
        }

        public List<string> WellKnownObjects()
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            ILdapSearchResults searchResults = connection.Search(DefaultNamingContext, LdapConnection.ScopeBase, "(objectclass=domain)", null, false);

            List<string> results = new List<string>();

            while (searchResults.HasMore())
            {
                var nextEntry = searchResults.Next();
                results = nextEntry.GetAttribute("wellKnownObjects").StringValueArray.ToList();
            }

            return results;
        }
        public RootDseInfo GetRootDse()
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            return connection.GetRootDseInfo();
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            connection?.Dispose();
        }

    }

}