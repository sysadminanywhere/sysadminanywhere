using SysadminAnywhere.ActiveDirectory.Models;
using SysadminAnywhere.ActiveDirectory.Repositories;
using SysadminAnywhere.ActiveDirectory.Services.Ldap;

internal class Program
{
    private static void Main(string[] args)
    {

        GenerateUsersAsync(1000).Wait();

    }

    private static async Task GenerateUsersAsync(int count)
    {

        ILdapService ldapService = new LdapService("192.168.245.132", 636, "admin", "Secret2#");
        UsersRepository repository = new UsersRepository(ldapService);

        for (int i = 0; i < count; i++)
        {

            UserEntry user = new UserEntry();

            user.Initials = Faker.Name.Middle();
            user.FirstName = Faker.Name.First();
            user.LastName = Faker.Name.Last();

            user.DisplayName = user.FirstName + " " + user.LastName;
            user.CN = user.DisplayName;

            user.SamAccountName = Faker.Internet.UserName();

            try
            {
                await repository.AddAsync("OU=Test,DC=example,DC=com", user, "aaa111#");
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
            }
        }

    }

}