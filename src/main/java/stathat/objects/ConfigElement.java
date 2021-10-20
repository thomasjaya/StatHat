package stathat.objects;

public class ConfigElement {
    /*
Object to store specific settings (e.g bool value would be used for toggling on/off).
Object used instead of different typed variables so can use arraylist of this object to save to file
 */
        private String name;
        private Float float_;
        private Integer int_;
        private Boolean bool_;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Float getFloat_() {
            return float_;
        }

        public void setFloat_(Float float_) {
            this.float_ = float_;
        }

        public Integer getInt_() {
            return int_;
        }

        public void setInt_(Integer int_) {
            this.int_ = int_;
        }

        public Boolean getBool_() { return bool_; }

        public void setBool_(Boolean bool_) {this.bool_ = bool_; }

}