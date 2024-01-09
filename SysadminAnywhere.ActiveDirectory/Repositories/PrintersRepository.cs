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

        public async Task<List<PrinterEntry>> ListAsync()
        {
            List<PrinterEntry> printers = new List<PrinterEntry>();

            List<LdapEntry> list = await ldapService.SearchAsync("(objectClass=printQueue)");

            foreach (LdapEntry entry in list)
            {
                printers.Add(ADResolver<PrinterEntry>.GetValues(entry));
            }

            return printers;
        }

        public async Task<PrinterEntry?> GetByCNAsync(string cn)
        {
            if (string.IsNullOrEmpty(cn))
                throw new ArgumentNullException(nameof(cn));

            var result = await ldapService.SearchAsync("(&(objectClass=printQueue)(cn=" + cn + "))");
            var entry = result.FirstOrDefault();

            if (entry != null)
                return ADResolver<PrinterEntry>.GetValues(entry);
            else
                return null;
        }

        public async Task DeleteAsync(PrinterEntry printer)
        {
            if (printer == null)
                throw new ArgumentNullException(nameof(printer));

            if (string.IsNullOrEmpty(printer.DistinguishedName))
                throw new ArgumentNullException(nameof(printer.DistinguishedName));

            await ldapService.DeleteAsync(printer.DistinguishedName);
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