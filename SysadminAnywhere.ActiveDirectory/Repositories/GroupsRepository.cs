using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;

namespace SysadminAnywhere.ActiveDirectory.Repositories
{
    public class GroupsRepository : IDisposable
    {

        private readonly ILdapService ldapService;

        public GroupsRepository(LdapService ldapService)
        {
            if (ldapService == null)
                throw new ArgumentNullException(nameof(ldapService));

            this.ldapService = ldapService;
        }

        public List<GroupEntry> List()
        {
            List<GroupEntry> groups = new List<GroupEntry>();

            List<LdapEntry> list = ldapService.Search("(objectClass=group)");

            foreach (LdapEntry entry in list)
            {
                groups.Add(ADResolver<GroupEntry>.GetValues(entry));
            }

            return groups;
        }

        public GroupEntry? GetByCN(string cn)
        {
            if (string.IsNullOrEmpty(cn))
                throw new ArgumentNullException(nameof(cn));

            var result = ldapService.Search("(&(objectClass=group)(cn=" + cn + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<GroupEntry>.GetValues(entry);
            else
                return null;
        }

        public GroupEntry? Add(GroupEntry group)
        {
            return Add(string.Empty, group, GroupScopes.Global, true);
        }

        public GroupEntry? Add(GroupEntry group, GroupScopes groupScope, bool isSecurity)
        {
            return Add(string.Empty, group, groupScope, isSecurity);
        }

        public GroupEntry? Add(string distinguishedName, GroupEntry group, GroupScopes groupScope, bool isSecurity)
        {
            if (group == null)
                throw new ArgumentNullException(nameof(group));

            if (string.IsNullOrEmpty(group.CN))
                throw new ArgumentNullException(nameof(group.CN));

            List<string> attributes = new List<string>
            {
                "description",
                "groupType",
                "sAMAccountName"
            };

            if (string.IsNullOrEmpty(group.SamAccountName))
                group.SamAccountName = group.CN;

            group.GroupType = GroupTypeExtensions.GetGroupType(groupScope, isSecurity);

            if (string.IsNullOrEmpty(distinguishedName))
            {
                string cn = "cn=" + group.CN + "," + new ADContainers(ldapService).GetUsersContainer();
                ldapService.Add(LdapResolver.GetLdapEntry(cn, group, attributes));
            }
            else
            {
                string cn = "cn=" + group.CN + "," + distinguishedName;
                ldapService.Add(LdapResolver.GetLdapEntry(cn, group, attributes));
            }

            var result = ldapService.Search("(&(objectClass=group)(cn=" + group.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<GroupEntry>.GetValues(entry);
            else
                return null;
        }

        public GroupEntry? Modify(GroupEntry group)
        {
            if (group == null)
                throw new ArgumentNullException(nameof(group));

            if (string.IsNullOrEmpty(group.CN))
                throw new ArgumentNullException(nameof(group.CN));

            if (string.IsNullOrEmpty(group.DistinguishedName))
                throw new ArgumentNullException(nameof(group.DistinguishedName));

            List<string> attributes = new List<string>
            {
                "description"
            };

            var result = ldapService.Search("(&(objectClass=group)(cn=" + group.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                GroupEntry oldGroup = ADResolver<GroupEntry>.GetValues(entry);
                ldapService.SendRequest(group.DistinguishedName, LdapResolver.GetModificationAttributes(group, oldGroup, attributes));

                var newGroup = GetByCN(group.CN);
                if (newGroup != null)
                    return newGroup;
            }

            return null;
        }

        public void Delete(GroupEntry group)
        {
            if (group == null)
                throw new ArgumentNullException(nameof(group));

            if (string.IsNullOrEmpty(group.DistinguishedName))
                throw new ArgumentNullException(nameof(group.DistinguishedName));

            ldapService.Delete(group.DistinguishedName);
        }

        public void AddMember(GroupEntry group, string distinguishedName)
        {
            if (group == null)
                throw new ArgumentNullException(nameof(group));

            if (string.IsNullOrEmpty(group.CN))
                throw new ArgumentNullException(nameof(group.CN));

            if (string.IsNullOrEmpty(group.DistinguishedName))
                throw new ArgumentNullException(nameof(group.DistinguishedName));

            if (string.IsNullOrEmpty(distinguishedName))
                throw new ArgumentNullException(nameof(distinguishedName));

            var result = ldapService.Search("(&(objectClass=group)(cn=" + group.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                LdapModification ldapModification = new LdapModification(LdapModification.Add, new LdapAttribute("member", distinguishedName));
                ldapService.SendRequest(group.DistinguishedName, ldapModification);
            }
        }

        public void DeleteMember(GroupEntry group, string distinguishedName)
        {
            if (group == null)
                throw new ArgumentNullException(nameof(group));

            if (string.IsNullOrEmpty(group.CN))
                throw new ArgumentNullException(nameof(group.CN));

            if (string.IsNullOrEmpty(group.DistinguishedName))
                throw new ArgumentNullException(nameof(group.DistinguishedName));

            if (string.IsNullOrEmpty(distinguishedName))
                throw new ArgumentNullException(nameof(distinguishedName));

            var result = ldapService.Search("(&(objectClass=group)(cn=" + group.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                LdapModification ldapModification = new LdapModification(LdapModification.Delete, new LdapAttribute("member"));
                ldapService.SendRequest(group.DistinguishedName, ldapModification);
            }
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            ldapService?.Dispose();
        }

    }
}