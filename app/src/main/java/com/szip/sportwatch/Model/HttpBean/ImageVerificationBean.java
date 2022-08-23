package com.szip.sportwatch.Model.HttpBean;

public class ImageVerificationBean extends BaseApi {


    private ImageVerificationData data;

    public ImageVerificationData getData() {
        return data;
    }

    public void setData(ImageVerificationData data) {
        this.data = data;
    }

    public class ImageVerificationData{
        private String id;
        private int validTime;
        private String inputImage;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getValidTime() {
            return validTime;
        }

        public void setValidTime(int validTime) {
            this.validTime = validTime;
        }

        public String getInputImage() {
            return inputImage;
        }

        public void setInputImage(String inputImage) {
            this.inputImage = inputImage;
        }
    }


}
