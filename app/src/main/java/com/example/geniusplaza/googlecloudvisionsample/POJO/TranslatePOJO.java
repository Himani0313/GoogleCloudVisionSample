package com.example.geniusplaza.googlecloudvisionsample.POJO;

import com.google.gson.annotations.SerializedName;

/**
 * Created by geniusplaza on 6/20/17.
 */
import com.google.gson.annotations.SerializedName;

    public class TranslatePOJO {
        @SerializedName("from")
        private String fromLang;

        @SerializedName("to")
        private String toLang;

        @SerializedName("text")
        private String text;

        @SerializedName("translationText")
        private String  translate;

        public String getFromLang() {
            return fromLang;
        }

        public String getToLang() {
            return toLang;
        }

        public String getText() {
            return text;
        }

        public String getTranslate() {
            return translate;
        }
}
