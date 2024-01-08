using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;
using System.Reflection;

namespace SysadminAnywhere.ActiveDirectory
{
    public static class LdapResolver
    {

        public static List<LdapModification> GetModificationAttributes(IADEntry entry, IADEntry entryOld, List<string> attributes)
        {
            List<LdapModification> modificationAttributes = new List<LdapModification>();

            List<PropertyInfo> properties = entry.GetType().GetRuntimeProperties().ToList();
            List<PropertyInfo> propertiesOld = entryOld.GetType().GetRuntimeProperties().ToList();

            foreach (PropertyInfo property in properties)
            {
                var adAttributes = (ADAttribute[])property.GetCustomAttributes(typeof(ADAttribute), true);

                if (adAttributes != null && adAttributes.Length > 0)
                {
                    var attribute = attributes.FirstOrDefault(c => c.ToLower() == adAttributes[0].Name.ToLower());

                    if (attribute != null)
                    {
                        object? value = property.GetValue(entry);
                        var propertyOld = propertiesOld.First(c => c.Name == property.Name);
                        object? valueOld = propertyOld.GetValue(entryOld);

                        if (value is string && string.IsNullOrEmpty(value.ToString()))
                            value = null;

                        if (value != null)
                        {
                            var ldapModOperation = LdapModification.Replace;

                            if (valueOld == null)
                                ldapModOperation = LdapModification.Add;

                            LdapAttribute ldapAttribute = new LdapAttribute(attribute, value.ToString());
                            LdapModification ldapModification = new LdapModification(ldapModOperation, ldapAttribute);

                            modificationAttributes.Add(ldapModification);
                        }
                        else
                        {
                            if (valueOld != null)
                            {
                                LdapAttribute ldapAttribute = new LdapAttribute(attribute);
                                LdapModification ldapModification = new LdapModification(LdapModification.Delete, ldapAttribute);
                                modificationAttributes.Add(ldapModification);
                            }
                        }

                    }
                }
            }

            return modificationAttributes;
        }

        public static LdapEntry GetLdapEntry(string dn, IADEntry entry, List<string> attributes)
        {

            LdapAttributeSet attrs = new LdapAttributeSet();

            List<PropertyInfo> properties = entry.GetType().GetRuntimeProperties().ToList();

            foreach (PropertyInfo property in properties)
            {
                var adAttributes = (ADAttribute[])property.GetCustomAttributes(typeof(ADAttribute), true);

                if (adAttributes != null && adAttributes.Length > 0)
                {
                    var attribute = attributes.FirstOrDefault(c => c.ToLower() == adAttributes[0].Name.ToLower());

                    if (attribute != null)
                    {
                        object? value = property.GetValue(entry);

                        if (value != null)
                        {
                            attrs.Add(new LdapAttribute(attribute, new List<string> { value.ToString() }.ToArray()));
                        }
                    }
                }
            }

            attrs.Add(new LdapAttribute("objectclass", entry.ObjectClass.ToArray()));

            LdapEntry ldapEntry = new LdapEntry(dn, attrs);

            return ldapEntry;
        }

    }
}