package com.delgo.reward.comm.exception;

public class API {
    public static class CODE {
        // -------------------------COMMON-------------------------
        public static final int SUCCESS = 200;

        public static final int PARAM_ERROR = 301;
        public static final int PARAM_DATE_ERROR = 302;

        // TOKEN ERROR
        public static final int TOKEN_ERROR = 303;

        // LOGIN ERROR
        public static final int LOGIN_ERROR = 304;

        // DB ERROR
        public static final int DB_ERROR = 305;
        public static final int DB_DELETE_ERROR = 306;
        public static final int NOT_FOUND_DATA = 307;

        // NCP ERROR
        public static final int PHOTO_UPLOAD_ERROR = 308;
        public static final int SMS_ERROR = 309;

        // PHOTO ERROR
        public static final int PHOTO_EXTENSION_ERROR = 310;

        // INVALID_USER_ERROR
        public static final int INVALID_USER_ERROR = 315;

        // ------------------------FUNCTION----------------------------

        // MUNGPLE ERROR
        public static final int MUNGPLE_DUPLICATE_ERROR = 311;

        // Certification ERROR
        public static final int TOO_FAR_DISTANCE = 312;
        public static final int CERTIFICATION_TIME_ERROR = 313;
        public static final int CERTIFICATION_CATEGPRY_COUNT_ERROR = 314;

        //PHONE NO ERROR
        public static final int PHONE_NO_NOT_EXIST = 370;
        public static final int PHONE_NO_DUPLICATE_ERROR = 371;

        //SMS AUTH ERROR
        public static final int AUTH_DO_NOT_MATCHING = 317;

        //OAuth ERROR
        public static final int OAUTH_PHONE_NO_NOT_EXIST = 380;
        public static final int ANOTHER_OAUTH_CONNECT = 381;
        public static final int APPLE_UNIQUE_NO_NOT_FOUND = 383;

        public static final int UNKNOWN_ERROR = 1000;
        public static final int SERVER_TIMEOUT_ERROR = 1001;
        public static final int SERVER_ERROR = 1003;
    }
}