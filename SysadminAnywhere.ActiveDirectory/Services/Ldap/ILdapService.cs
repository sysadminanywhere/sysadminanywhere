using Novell.Directory.Ldap;

namespace SysadminAnywhere.ActiveDirectory.Services.Ldap
{
    public interface ILdapService
    {

        string DefaultNamingContext { get; set; }
        string DomainName { get; set; }

        bool IsConnected();

        List<LdapEntry> Search(string filter);
        List<LdapEntry> Search(string path, string filter, int scope = LdapConnection.ScopeSub);

        void SendRequest(string dn, List<LdapModification> ldapModifications);
        void SendRequest(string dn, LdapModification ldapModification);

        void Add(LdapEntry entry);

        void Modify(string dn, LdapModification entry);
        void ModifyProperty(string dn, string name, string value);

        void Delete(string dn);

        List<string> WellKnownObjects();
        RootDseInfo GetRootDse();

        void Dispose();
    }
}
