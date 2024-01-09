using Novell.Directory.Ldap;

namespace SysadminAnywhere.ActiveDirectory.Services.Ldap
{
    public interface ILdapService
    {

        string DefaultNamingContext { get; set; }
        string DomainName { get; set; }

        bool IsConnected();

        Task<List<LdapEntry>> SearchAsync(string filter);
        Task<List<LdapEntry>> SearchAsync(string path, string filter, int scope = LdapConnection.ScopeSub);

        Task SendRequestAsync(string dn, List<LdapModification> ldapModifications);
        Task SendRequestAsync(string dn, LdapModification ldapModification);

        Task AddAsync(LdapEntry entry);

        Task ModifyAsync(string dn, LdapModification entry);
        Task ModifyPropertyAsync(string dn, string name, string value);

        Task DeleteAsync(string dn);

        Task<List<string>> WellKnownObjectsAsync();
        Task<RootDseInfo> GetRootDseAsync();

        void Dispose();
    }
}
