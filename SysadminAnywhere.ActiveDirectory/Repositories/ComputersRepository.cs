using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;

namespace SysadminAnywhere.ActiveDirectory.Repositories
{
    public class ComputersRepository : IDisposable
    {

        private readonly ILdapService ldapService;

        public ComputersRepository(ILdapService ldapService)
        {
            if (ldapService == null)
                throw new ArgumentNullException(nameof(ldapService));

            this.ldapService = ldapService;
        }

        public List<ComputerEntry> List()
        {
            List<ComputerEntry> computers = new List<ComputerEntry>();

            List<LdapEntry> list = ldapService.Search("(objectClass=computer)");

            foreach (LdapEntry entry in list)
            {
                computers.Add(ADResolver<ComputerEntry>.GetValues(entry));
            }

            return computers;
        }

        public ComputerEntry? GetByCN(string cn)
        {
            if (string.IsNullOrEmpty(cn))
                throw new ArgumentNullException(nameof(cn));

            var result = ldapService.Search("(&(objectClass=computer)(cn=" + cn + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<ComputerEntry>.GetValues(entry);
            else
                return null;
        }

        public ComputerEntry? Add(ComputerEntry computer, bool isEnabled)
        {
            if (computer == null)
                throw new ArgumentNullException(nameof(computer));

            return Add(string.Empty, computer, isEnabled);
        }

        public ComputerEntry? Add(string distinguishedName, ComputerEntry computer, bool isEnabled)
        {
            if (computer == null)
                throw new ArgumentNullException(nameof(computer));

            if (string.IsNullOrEmpty(computer.CN))
                throw new ArgumentNullException(nameof(computer.CN));


            List<string> attributes = new List<string>
            {
                "description",
                "location",
                "sAMAccountName"
            };

            if (string.IsNullOrEmpty(computer.SamAccountName))
                computer.SamAccountName = computer.CN;

            if (string.IsNullOrEmpty(distinguishedName))
            {
                string cn = "cn=" + computer.CN + "," + new ADContainers(ldapService).GetUsersContainer();
                ldapService.Add(LdapResolver.GetLdapEntry(cn, computer, attributes));
            }
            else
            {
                string cn = "cn=" + computer.CN + "," + distinguishedName;
                ldapService.Add(LdapResolver.GetLdapEntry(cn, computer, attributes));
            }

            var result = ldapService.Search("(&(objectClass=computer)(cn=" + computer.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                ComputerEntry newComputer = ADResolver<ComputerEntry>.GetValues(entry);

                UserAccountControls userAccountControl = newComputer.UserControl;

                if (!isEnabled)
                {
                    if ((userAccountControl & UserAccountControls.ACCOUNTDISABLE) != UserAccountControls.ACCOUNTDISABLE)
                        userAccountControl = userAccountControl & UserAccountControls.ACCOUNTDISABLE;
                }
                else
                {
                    if ((userAccountControl & UserAccountControls.ACCOUNTDISABLE) == UserAccountControls.ACCOUNTDISABLE)
                        userAccountControl = userAccountControl & ~UserAccountControls.ACCOUNTDISABLE;
                }

                ldapService.ModifyProperty(newComputer.DistinguishedName, "userAccountControl", Convert.ToString((int)userAccountControl));

                return newComputer;
            }

            return null;
        }

        public ComputerEntry? Modify(ComputerEntry computer)
        {
            if (computer == null)
                throw new ArgumentNullException(nameof(computer));

            if (string.IsNullOrEmpty(computer.CN))
                throw new ArgumentNullException(nameof(computer.CN));

            if (string.IsNullOrEmpty(computer.DistinguishedName))
                throw new ArgumentNullException(nameof(computer.DistinguishedName));

            List<string> attributes = new List<string>
            {
                "description",
                "location"
            };

            var result = ldapService.Search("(&(objectClass=computer)(cn=" + computer.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                ComputerEntry oldComputer = ADResolver<ComputerEntry>.GetValues(entry);
                ldapService.SendRequest(computer.DistinguishedName, LdapResolver.GetModificationAttributes(computer, oldComputer, attributes));

                var newComputer = GetByCN(computer.CN);
                if (newComputer != null)
                    return newComputer;
            }
            return null;
        }

        public void Delete(ComputerEntry computer)
        {
            if (computer == null)
                throw new ArgumentNullException(nameof(computer));

            if (string.IsNullOrEmpty(computer.DistinguishedName))
                throw new ArgumentNullException(nameof(computer.DistinguishedName));

            ldapService.Delete(computer.DistinguishedName);
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