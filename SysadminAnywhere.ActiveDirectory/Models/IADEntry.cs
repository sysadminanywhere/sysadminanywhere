namespace SysadminAnywhere.ActiveDirectory.Models
{
    public interface IADEntry
    {
        string CN { get; set; }

        List<string> ObjectClass { get; set; }

    }
}
