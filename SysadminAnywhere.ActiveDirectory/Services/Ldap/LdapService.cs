using Novell.Directory.Ldap;
using static System.Formats.Asn1.AsnWriter;
using System.IO;
using Novell.Directory.Ldap.Controls;
using Novell.Directory.Ldap.SearchExtensions;
using System.Net.Http.Headers;

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
                    connection.SecureSocketLayer = true;
                }
                else {
                    connection = new LdapConnection();
                }

                connection.ConnectAsync(host, port).Wait();
                connection.BindAsync(userName, password).Wait();

                DefaultNamingContext = connection.GetRootDseInfoAsync().Result.DefaultNamingContext;
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

        public async Task<List<LdapEntry>> SearchAsync(string filter)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(filter))
                throw new ArgumentNullException(nameof(filter));

            return await SearchAsync(DefaultNamingContext, filter);
        }

        public async Task<List<LdapEntry>> SearchAsync(string path, string filter, int scope = LdapConnection.ScopeSub)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(path))
                throw new ArgumentNullException(nameof(path));

            if (string.IsNullOrEmpty(filter))
                throw new ArgumentNullException(nameof(filter));

            var vlvControlHandler = new VirtualListViewControlHandler(connection);
            var sortControl = new LdapSortControl(new LdapSortKey("cn"), true);

            SearchOptions options = new SearchOptions(path, scope, filter, null);

            return await vlvControlHandler.SearchUsingVlvAsync(sortControl, options, 100);

        }

        public async Task SendRequestAsync(string dn, List<LdapModification> ldapModifications)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (ldapModifications == null)
                throw new ArgumentNullException(nameof(ldapModifications));

            await connection.ModifyAsync(dn, ldapModifications.ToArray());
        }

        public async Task SendRequestAsync(string dn, LdapModification ldapModification)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (ldapModification == null)
                throw new ArgumentNullException(nameof(ldapModification));

            await connection.ModifyAsync(dn, ldapModification);
        }

        public async Task AddAsync(LdapEntry entry)
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

            await connection.AddAsync(entry);
        }

        public async Task ModifyAsync(string dn, LdapModification entry)
        {
            if (connection == null)
                throw new NullReferenceException(nameof(connection));

            if (entry == null)
                throw new ArgumentNullException(nameof(entry));

            await connection.ModifyAsync(dn, entry);
        }

        public async Task ModifyPropertyAsync(string dn, string name, string value)
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

            await connection.ModifyAsync(dn, ldapModification);
        }

        public async Task DeleteAsync(string dn)
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            if (string.IsNullOrEmpty(dn))
                throw new ArgumentNullException(nameof(dn));

            await connection.DeleteAsync(dn);
        }

        public async Task<List<string>> WellKnownObjectsAsync()
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            ILdapSearchResults searchResults = await connection.SearchAsync(DefaultNamingContext, LdapConnection.ScopeBase, "(objectclass=domain)", null, false);

            List<string> results = new List<string>();

            var result = await searchResults.ToListAsync<LdapEntry>();

            foreach (LdapEntry ldapEntry in result) 
            { 
                results = ldapEntry.GetAttribute("wellKnownObjects").StringValueArray.ToList();
            }

            return results;
        }
        public async Task<RootDseInfo> GetRootDseAsync()
        {
            if (connection == null)
                throw new ArgumentNullException(nameof(connection));

            return await connection.GetRootDseInfoAsync();
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