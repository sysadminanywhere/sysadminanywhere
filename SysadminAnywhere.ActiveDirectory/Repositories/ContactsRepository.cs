using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;

namespace SysadminAnywhere.ActiveDirectory.Repositories
{
    public class ContactsRepository : IDisposable
    {

        private readonly ILdapService ldapService;

        public ContactsRepository(ILdapService ldapService)
        {
            if (ldapService == null)
                throw new ArgumentNullException(nameof(ldapService));

            this.ldapService = ldapService;
        }

        public List<ContactEntry> ListAsync()
        {
            List<ContactEntry> contacts = new List<ContactEntry>();

            List<LdapEntry> list = ldapService.Search("(&(objectClass=contact)(objectCategory=person))");

            foreach (LdapEntry entry in list)
            {
                contacts.Add(ADResolver<ContactEntry>.GetValues(entry));
            }

            return contacts;
        }

        public ContactEntry? GetByCN(string cn)
        {
            if (string.IsNullOrEmpty(cn))
                throw new ArgumentNullException(nameof(cn));

            var result = ldapService.Search("(&(objectClass=contact)(objectCategory=person)(cn=" + cn + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<ContactEntry>.GetValues(entry);
            else
                return null;
        }

        public ContactEntry? Add(ContactEntry contact)
        {
            return Add(string.Empty, contact);
        }

        public ContactEntry? Add(string distinguishedName, ContactEntry contact)
        {
            if (contact == null)
                throw new ArgumentNullException(nameof(contact));

            if (string.IsNullOrEmpty(contact.CN))
                throw new ArgumentNullException(nameof(contact.CN));

            List<string> attributes = new List<string>
            {
                "displayName",
                "initials",
                "givenName",
                "sn"
            };

            if (string.IsNullOrEmpty(distinguishedName))
            {
                string cn = "cn=" + contact.CN + "," + new ADContainers(ldapService).GetUsersContainer();
                ldapService.Add(LdapResolver.GetLdapEntry(cn, contact, attributes));
            }
            else
            {
                string cn = "cn=" + contact.CN + "," + distinguishedName;
                ldapService.Add(LdapResolver.GetLdapEntry(cn, contact, attributes));
            }

            var result = ldapService.Search("(&(objectClass=contact)(objectCategory=person)(cn=" + contact.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<ContactEntry>.GetValues(entry);
            else
                return null;
        }

        public ContactEntry? Modify(ContactEntry contact)
        {
            if (contact == null)
                throw new ArgumentNullException(nameof(contact));

            if (string.IsNullOrEmpty(contact.CN))
                throw new ArgumentNullException(nameof(contact.CN));

            if (string.IsNullOrEmpty(contact.DistinguishedName))
                throw new ArgumentNullException(nameof(contact.DistinguishedName));

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

            var result = ldapService.Search("(&(objectClass=contact)(objectCategory=person)(cn=" + contact.CN + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
            {
                ContactEntry oldContact = ADResolver<ContactEntry>.GetValues(entry);
                ldapService.SendRequest(contact.DistinguishedName, LdapResolver.GetModificationAttributes(contact, oldContact, attributes));

                var newContact = GetByCN(contact.CN);
                if (newContact != null)
                    return newContact;
            }

            return null;
        }

        public void Delete(ContactEntry contact)
        {
            if (contact == null)
                throw new ArgumentNullException(nameof(contact));

            if (string.IsNullOrEmpty(contact.DistinguishedName))
                throw new ArgumentNullException(nameof(contact.DistinguishedName));

            ldapService.Delete(contact.DistinguishedName);
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