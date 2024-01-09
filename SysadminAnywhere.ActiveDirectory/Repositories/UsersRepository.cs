using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;
using System.Runtime.InteropServices;
using System.Text;

namespace SysadminAnywhere.ActiveDirectory.Repositories
{
    public class UsersRepository : IDisposable
    {

        private readonly ILdapService ldapService;

        public UsersRepository(ILdapService ldapService)
        {
            if (ldapService == null)
                throw new ArgumentNullException(nameof(ldapService));

            this.ldapService = ldapService;
        }

        public async Task<List<UserEntry>> ListAsync()
        {
            List<UserEntry> users = new List<UserEntry>();

            List<LdapEntry> list = await ldapService.SearchAsync("(&(objectClass=user)(objectCategory=person))");

            foreach (LdapEntry entry in list)
            {
                users.Add(ADResolver<UserEntry>.GetValues(entry));
            }

            return users;
        }

        public async Task<UserEntry?> GetByCNAsync(string cn)
        {
            if (string.IsNullOrEmpty(cn))
                throw new ArgumentNullException(nameof(cn));

            var result = await ldapService.SearchAsync("(&(objectClass=user)(objectCategory=person)(cn=" + cn + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<UserEntry>.GetValues(entry);
            else
                return null;
        }

        public async Task<UserEntry?> AddAsync(UserEntry user, string password)
        {
            return await AddAsync(string.Empty, user, password, false, false, false, false);
        }

        public async Task<UserEntry?> AddAsync(string distinguishedName, UserEntry user, string password)
        {
            return await AddAsync(distinguishedName, user, password, false, false, false, false);
        }

        public async Task<UserEntry?> AddAsync(string distinguishedName, UserEntry user, string password, bool isCannotChangePassword, bool isPasswordNeverExpires, bool isAccountDisabled, bool isMustChangePassword)
        {
            if (user == null)
                throw new ArgumentNullException(nameof(user));

            if (string.IsNullOrEmpty(user.CN))
                throw new ArgumentNullException(nameof(user.CN));

            if (string.IsNullOrEmpty(password))
                throw new ArgumentNullException(nameof(password));

            if (string.IsNullOrEmpty(user.UserPrincipalName))
                user.UserPrincipalName = user.SamAccountName + "@" + ldapService.DomainName;

            List<string> attributes = new List<string>
            {
                "displayName",
                "initials",
                "givenName",
                "sn",
                "sAMAccountName",
                "userPrincipalName"
            };

            if (string.IsNullOrEmpty(distinguishedName))
            {
                string cn = "cn=" + user.CN + "," + new ADContainers(ldapService).GetUsersContainer();
                await ldapService.AddAsync(LdapResolver.GetLdapEntry(cn, user, attributes));
            }
            else
            {
                string cn = "cn=" + user.CN + "," + distinguishedName;
                await ldapService.AddAsync(LdapResolver.GetLdapEntry(cn, user, attributes));
            }

            var result = await ldapService.SearchAsync("(&(objectClass=user)(objectCategory=person)(cn=" + user.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                UserEntry newUser = ADResolver<UserEntry>.GetValues(entry);

                await ResetPasswordAsync(newUser, password);

                await ChangeUserAccountControlAsync(newUser, isCannotChangePassword, isPasswordNeverExpires, isAccountDisabled);

                if (isMustChangePassword)
                    await MustChangePasswordAsync(newUser);

                return newUser;
            }

            return null;
        }

        public async Task AddAsync(string distinguishedName, UserEntry user)
        {
            if (user == null)
                throw new ArgumentNullException(nameof(user));

            if (string.IsNullOrEmpty(user.CN))
                throw new ArgumentNullException(nameof(user.CN));

            if (string.IsNullOrEmpty(user.UserPrincipalName))
                user.UserPrincipalName = user.SamAccountName + "@" + ldapService.DomainName;

            List<string> attributes = new List<string>
            {
                "displayName",
                "initials",
                "givenName",
                "sn",
                "sAMAccountName",
                "userPrincipalName"
            };

            if (string.IsNullOrEmpty(distinguishedName))
            {
                string cn = "cn=" + user.CN + "," + new ADContainers(ldapService).GetUsersContainer();
                await ldapService.AddAsync(LdapResolver.GetLdapEntry(cn, user, attributes));
            }
            else
            {
                string cn = "cn=" + user.CN + "," + distinguishedName;
                await ldapService.AddAsync(LdapResolver.GetLdapEntry(cn, user, attributes));
            }
        }

        public async Task<UserEntry?> ModifyAsync(UserEntry user)
        {
            if (user == null)
                throw new ArgumentNullException(nameof(user));

            if (string.IsNullOrEmpty(user.CN))
                throw new ArgumentNullException(nameof(user.CN));

            if (string.IsNullOrEmpty(user.DistinguishedName))
                throw new ArgumentNullException(nameof(user.DistinguishedName));


            List<string> attributes = new List<string>
            {
                "displayName",
                "initials",
                "givenName",
                "sn",
                "description",
                "physicalDeliveryOfficeName",
                "telephoneNumber",
                "mail",
                "wWWHomePage",
                "streetAddress",
                "postOfficeBox",
                "l",
                "st",
                "postalCode",
                "homePhone",
                "mobile",
                "facsimileTelephoneNumber",
                "title",
                "department",
                "company"
            };

            var result = await ldapService.SearchAsync("(&(objectClass=user)(objectCategory=person)(cn=" + user.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                UserEntry oldUser = ADResolver<UserEntry>.GetValues(entry);
                await ldapService.SendRequestAsync(user.DistinguishedName, LdapResolver.GetModificationAttributes(user, oldUser, attributes));

                var newUser = await GetByCNAsync(user.CN);
                if (newUser != null)
                    return newUser;
            }

            return null;
        }

        public async Task ResetPasswordAsync(UserEntry user, string password)
        {
            byte[] encodedBytes = Encoding.Unicode.GetBytes("\"" + password + "\"");

            LdapModification ldapModification = new LdapModification(LdapModification.Replace, new LdapAttribute("unicodePwd", encodedBytes));
            await ldapService.SendRequestAsync(user.DistinguishedName, ldapModification);
        }

        public async Task ChangeUserAccountControlAsync(UserEntry user, bool isCannotChangePassword, bool isPasswordNeverExpires, bool isAccountDisabled)
        {
            UserAccountControls userAccountControl = user.UserControl;

            if (isCannotChangePassword)
                userAccountControl = userAccountControl | UserAccountControls.PASSWD_CANT_CHANGE;
            else
                userAccountControl = userAccountControl & ~UserAccountControls.PASSWD_CANT_CHANGE;

            if (isPasswordNeverExpires)
                userAccountControl = userAccountControl | UserAccountControls.DONT_EXPIRE_PASSWD;
            else
                userAccountControl = userAccountControl & ~UserAccountControls.DONT_EXPIRE_PASSWD;

            if (isAccountDisabled)
                userAccountControl = userAccountControl | UserAccountControls.ACCOUNTDISABLE;
            else
                userAccountControl = userAccountControl & ~UserAccountControls.ACCOUNTDISABLE;

            await ldapService.ModifyPropertyAsync(user.DistinguishedName, "userAccountControl", Convert.ToString((int)userAccountControl));
        }

        public async Task MustChangePasswordAsync(UserEntry user)
        {
            await ldapService.ModifyPropertyAsync(user.DistinguishedName, "pwdlastset", "0");
        }

        public async Task DeleteAsync(UserEntry user)
        {
            if (user == null)
                throw new ArgumentNullException(nameof(user));

            if (string.IsNullOrEmpty(user.DistinguishedName))
                throw new ArgumentNullException(nameof(user.DistinguishedName));

            await ldapService.DeleteAsync(user.DistinguishedName);
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