package com.efi.PrintMeGoogle.constants;

public class GeneralConstants {


    public static final String EMAIL_HASH_PREF = "emailHash_pref";
    public static final String EMAIL_HASH = "emailHash";
    public static final String OWNER_EMAIL_PREF = "ownerEmail_pref";
    public static final String OWNER_EMAIL = "ownerEmail";
    public static final String REGISTERED_PREF = "registered_pref";
    public static final String REGISTERED = "registered";

    public static final String NOTIFY_PREF = "notification_pref";
    public static final String NOTIFY = "notification";
    public static final String UNIQUE_ID_PREF = "unique_id_pref";
    public static final String UNIQUE_ID = "unique_id";
    public static final String EMAIL_RESPONSE_STATUS_PREF = "email_response_status_pref";
    public static final String EMAIL_RESPONSE_STATUS = "email_response_status";
    public static final String NOTIFY_STATUS_PREF = "notify_status_pref";
    public static final String NOTIFY_STATUS = "notify_status";

    public static final String PM2SerialHeaderKey = "PrintMe-Serial";
    public static final String PM2DRNsPath = "/api/v2/drns";
    public static final String PM2DocumentsPath = "/api/v2/documents";
    public static final String PM2MapPath = "/map";
    public static final String PM2RegistrationPath = "/api/v2/emailregistrations/mobile";
    public static final String PM2ListDRNsGetPath = "/api/v2/drns/reference";
    public static final String PM2ReRegistartionPath = "/api/v2/emailregistrations/mobile/reregister/";
    public static final String Client_Secret = "3Q85o29!P$Akpt5@";
    public static final String Client_Id = "7e3785cd-c92d-4a82-bead-41aa3deaf5d7";
    public static final String TermsOfUseUrl ="https://www.fiery.com/legal/terms-of-use/";// "https://product-redirect.efi.com/terms-of-use";
    public static final String PrivacyPolicyUrl = "https://www.fiery.com/legal/privacy/#info-we-collect";//https://product-redirect.efi.com/privacy";
    public static final String HelpUrl = "https://prd2.printme.com/faq";
    public static String LocaleCode = "en";
    public static final String PM2APIBasePath = "https://prd2.printme.com";
    public static final String DefaultCustomEmail = "print@printmeservice.com";
    public static final int UploadClientIdAndroid = 6;
    public static String serial_number = "unknown";
    public static final String AppLog = "***PrintMeMobile****";
    public static final String Terms = "terms";
    public static final String Privacy = "privacy";
    public static final String Help = "help";
    public static final String PageName = "page_name";

    public static String getLocaleCode() {
        return LocaleCode;
    }

    public static void setLocaleCode(String localeCode) {
        LocaleCode = localeCode;
    }

    public static boolean Uploading = false;


    public static String getSerial_number() {
        return serial_number;
    }

    public static void setSerial_number(String serial_number) {
        GeneralConstants.serial_number = serial_number;
    }

    public static boolean getUploading() {
        return Uploading;
    }

    public static void setUploading(boolean uploading) {
        GeneralConstants.Uploading = uploading;
    }
}


