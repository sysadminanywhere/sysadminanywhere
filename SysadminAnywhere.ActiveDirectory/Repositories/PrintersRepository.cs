using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;

namespace SysadminAnywhere.ActiveDirectory.Repositories
{
    public class PrintersRepository : IDisposable
    {

        private readonly ILdapService ldapService;

        public PrintersRepository(ILdapService ldapService)
        {
            if (ldapService == null)
                throw new ArgumentNullException(nameof(ldapService));

            this.ldapService = ldapService;
        }

        public List<PrinterEntry> List()
        {
            List<PrinterEntry> printers = new List<PrinterEntry>();

            List<LdapEntry> list = ldapService.Search("(objectClass=printQueue)");

            foreach (LdapEntry entry in list)
            {
                printers.Add(ADResolver<PrinterEntry>.GetValues(entry));
            }

            return printers;
        }

        public PrinterEntry? GetByCN(string cn)
        {
            if (string.IsNullOrEmpty(cn))
                throw new ArgumentNullException(nameof(cn));

            var result = ldapService.Search("(&(objectClass=printQueue)(cn=" + cn + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<PrinterEntry>.GetValues(entry);
            else
                return null;
        }

        public void Delete(PrinterEntry printer)
        {
            if (printer == null)
                throw new ArgumentNullException(nameof(printer));

            if (string.IsNullOrEmpty(printer.DistinguishedName))
                throw new ArgumentNullException(nameof(printer.DistinguishedName));

            ldapService.Delete(printer.DistinguishedName);
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