package smartlink.zhy.jyfridge.bean;


import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/30 0030.
 */

public class BaseEntity {

    private int code;
    private String msg;
    private ResultBeanX result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResultBeanX getResult() {
        return result;
    }

    public void setResult(ResultBeanX result) {
        this.result = result;
    }

    public static class ResultBeanX {

        private String _text;
        private String msg_id;
        private int meta_process_milliseconds;
        private List<IntentsBean> intents;

        public String get_text() {
            return _text;
        }

        public void set_text(String _text) {
            this._text = _text;
        }

        public String getMsg_id() {
            return msg_id;
        }

        public void setMsg_id(String msg_id) {
            this.msg_id = msg_id;
        }

        public int getMeta_process_milliseconds() {
            return meta_process_milliseconds;
        }

        public void setMeta_process_milliseconds(int meta_process_milliseconds) {
            this.meta_process_milliseconds = meta_process_milliseconds;
        }

        public List<IntentsBean> getIntents() {
            return intents;
        }

        public void setIntents(List<IntentsBean> intents) {
            this.intents = intents;
        }

        public static class IntentsBean {

            private Map<String,Object> parameters;
            private String action;
            private String name;
            private ResultBean result;
            private String score;
            private String scoreColor;
            private int is_match;
            private String id;
            private List<OutputsBean> outputs;

            public Map<String, Object> getParameters() {
                return parameters;
            }

            public void setParameters(Map<String, Object> parameters) {
                this.parameters = parameters;
            }

            public String getAction() {
                return action;
            }

            public void setAction(String action) {
                this.action = action;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public ResultBean getResult() {
                return result;
            }

            public void setResult(ResultBean result) {
                this.result = result;
            }

            public String getScore() {
                return score;
            }

            public void setScore(String score) {
                this.score = score;
            }

            public String getScoreColor() {
                return scoreColor;
            }

            public void setScoreColor(String scoreColor) {
                this.scoreColor = scoreColor;
            }

            public int getIs_match() {
                return is_match;
            }

            public void setIs_match(int is_match) {
                this.is_match = is_match;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public List<OutputsBean> getOutputs() {
                return outputs;
            }

            public void setOutputs(List<OutputsBean> outputs) {
                this.outputs = outputs;
            }

            public static class ResultBean {

                private String text;
                private String type;

                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }
            }

            public static class OutputsBean {

                private String type;
                private PropertyBean property;

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public PropertyBean getProperty() {
                    return property;
                }

                public void setProperty(PropertyBean property) {
                    this.property = property;
                }

                public static class PropertyBean {

                    private String text;

                    public String getText() {
                        return text;
                    }

                    public void setText(String text) {
                        this.text = text;
                    }
                }
            }
        }
    }
}
