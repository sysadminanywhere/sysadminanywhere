using Novell.Directory.Ldap;
using SysadminAnywhere.ActiveDirectory.Models;
using System.Diagnostics;
using System.Reflection;

namespace SysadminAnywhere.ActiveDirectory
{
    public static class ADResolver<T>
    {

        public static T GetValues(LdapEntry entry)
        {

            T result = (T)Activator.CreateInstance(typeof(T));

            Dictionary<string, LdapAttribute> directoryAttributes = entry.GetAttributeSet();


            foreach (PropertyInfo property in result.GetType().GetRuntimeProperties())
            {

                string propertyName = property.Name;
                ADAttribute.DateTypes dateType = ADAttribute.DateTypes.None;

                var attributes = (ADAttribute[])property.GetCustomAttributes(typeof(ADAttribute), true);

                if (attributes != null && attributes.Any())
                {
                    propertyName = attributes[0].Name;
                    dateType = attributes[0].DateType;
                }

                try
                {

                    var attribute = directoryAttributes.FirstOrDefault(a => a.Key.ToLower() == propertyName.ToLower());

                    if (attribute.Key != null)
                    {

                        if (property.PropertyType == typeof(string))
                            property.SetValue(result, attribute.Value.StringValue);

                        if (property.PropertyType == typeof(DateTime?))
                            property.SetValue(result, GetDate(attribute.Value.StringValue, dateType));

                        if (property.PropertyType == typeof(bool))
                            property.SetValue(result, bool.Parse(attribute.Value.StringValue));

                        if (property.PropertyType == typeof(int))
                            property.SetValue(result, int.Parse(attribute.Value.StringValue));

                        if (property.PropertyType == typeof(long))
                            property.SetValue(result, long.Parse(attribute.Value.StringValue));

                        if (property.PropertyType == typeof(byte[]))
                            property.SetValue(result, attribute.Value.ByteValue);

                        if (property.PropertyType == typeof(List<string>))
                            property.SetValue(result, attribute.Value.StringValueArray.ToList());

                        if (property.PropertyType == typeof(Guid))
                            property.SetValue(result, new Guid(attribute.Value.ByteValue));

                        if (property.PropertyType == typeof(ADSID))
                            property.SetValue(result, new ADSID(attribute.Value.ByteValue));

                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine("Error parsing: " + ex.Message);
                }
            }

            return result;
        }

        private static DateTime GetDate(string sDate, ADAttribute.DateTypes dateType)
        {
            if (sDate == "0")
                return new DateTime(1601, 01, 01, 0, 0, 0, DateTimeKind.Utc);

            if (dateType == ADAttribute.DateTypes.Date)
            {
                if (sDate.EndsWith('Z'))
                {
                    int year = Convert.ToInt32(sDate.Substring(0, 4));
                    int month = Convert.ToInt32(sDate.Substring(4, 2));
                    int day = Convert.ToInt32(sDate.Substring(6, 2));

                    int hour = 0;
                    int minute = 0;
                    int second = 0;

                    if (sDate.Length > 8)
                    {
                        hour = Convert.ToInt32(sDate.Substring(8, 2));
                        minute = Convert.ToInt32(sDate.Substring(10, 2));
                        second = Convert.ToInt32(sDate.Substring(12, 2));
                    }
                    return new DateTime(year, month, day, hour, minute, second);
                }
                else
                {
                    return Convert.ToDateTime(sDate);
                }
            }
            else
            {
                if (sDate.Length == 18)
                    return DateTime.FromFileTime(long.Parse(sDate));
                else
                    return new DateTime(1601, 01, 01, 0, 0, 0, DateTimeKind.Utc);
            }
        }

    }
}