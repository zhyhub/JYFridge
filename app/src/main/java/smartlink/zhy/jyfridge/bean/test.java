package smartlink.zhy.jyfridge.bean;

import java.util.List;

/**
 * Created by Administrator on 2017/12/5 0005.
 */

public class test {


    /**
     * code : 0
     * msg : ok
     * result : {"_text":"智能模式","msg_id":"00511e55-7cfe-4469-8bef-9d4f3ee16975","intents":[{"parameters":{"attr_value":"智能模式","operation":"set","attr":"mode","service":"fridge_smart_home"},"name":"设置-冰箱-模式","result":{"text":"已帮您将冰箱设置为智能模式","type":"dialog"},"outputs":[{"type":"wechat.text","property":{"text":"已帮您将冰箱设置为智能模式"}},{"type":"dialog","property":{"text":"已帮您将冰箱设置为智能模式","emotion":"calm"}}],"score":"1.0","scoreColor":"c4","is_match":1,"skill_id":"1d2c1fa3-153b-4758-8201-1b802f9f78ea","id":"a01f6ddc-a817-4a56-be9b-674ca2240fd7","action":"dialog"}],"meta_process_milliseconds":68}
     */

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
        /**
         * _text : 智能模式
         * msg_id : 00511e55-7cfe-4469-8bef-9d4f3ee16975
         * intents : [{"parameters":{"attr_value":"智能模式","operation":"set","attr":"mode","service":"fridge_smart_home"},"name":"设置-冰箱-模式","result":{"text":"已帮您将冰箱设置为智能模式","type":"dialog"},"outputs":[{"type":"wechat.text","property":{"text":"已帮您将冰箱设置为智能模式"}},{"type":"dialog","property":{"text":"已帮您将冰箱设置为智能模式","emotion":"calm"}}],"score":"1.0","scoreColor":"c4","is_match":1,"skill_id":"1d2c1fa3-153b-4758-8201-1b802f9f78ea","id":"a01f6ddc-a817-4a56-be9b-674ca2240fd7","action":"dialog"}]
         * meta_process_milliseconds : 68
         */

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
            /**
             * parameters : {"attr_value":"智能模式","operation":"set","attr":"mode","service":"fridge_smart_home"}
             * name : 设置-冰箱-模式
             * result : {"text":"已帮您将冰箱设置为智能模式","type":"dialog"}
             * outputs : [{"type":"wechat.text","property":{"text":"已帮您将冰箱设置为智能模式"}},{"type":"dialog","property":{"text":"已帮您将冰箱设置为智能模式","emotion":"calm"}}]
             * score : 1.0
             * scoreColor : c4
             * is_match : 1
             * skill_id : 1d2c1fa3-153b-4758-8201-1b802f9f78ea
             * id : a01f6ddc-a817-4a56-be9b-674ca2240fd7
             * action : dialog
             */

            private ParametersBean parameters;
            private String name;
            private ResultBean result;
            private String score;
            private String scoreColor;
            private int is_match;
            private String skill_id;
            private String id;
            private String action;
            private List<OutputsBean> outputs;

            public ParametersBean getParameters() {
                return parameters;
            }

            public void setParameters(ParametersBean parameters) {
                this.parameters = parameters;
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

            public String getSkill_id() {
                return skill_id;
            }

            public void setSkill_id(String skill_id) {
                this.skill_id = skill_id;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getAction() {
                return action;
            }

            public void setAction(String action) {
                this.action = action;
            }

            public List<OutputsBean> getOutputs() {
                return outputs;
            }

            public void setOutputs(List<OutputsBean> outputs) {
                this.outputs = outputs;
            }

            public static class ParametersBean {
                /**
                 * attr_value : 智能模式
                 * operation : set
                 * attr : mode
                 * service : fridge_smart_home
                 */

                private String attr_value;
                private String operation;
                private String attr;
                private String service;

                public String getAttr_value() {
                    return attr_value;
                }

                public void setAttr_value(String attr_value) {
                    this.attr_value = attr_value;
                }

                public String getOperation() {
                    return operation;
                }

                public void setOperation(String operation) {
                    this.operation = operation;
                }

                public String getAttr() {
                    return attr;
                }

                public void setAttr(String attr) {
                    this.attr = attr;
                }

                public String getService() {
                    return service;
                }

                public void setService(String service) {
                    this.service = service;
                }
            }

            public static class ResultBean {
                /**
                 * text : 已帮您将冰箱设置为智能模式
                 * type : dialog
                 */

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
                /**
                 * type : wechat.text
                 * property : {"text":"已帮您将冰箱设置为智能模式"}
                 */

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
                    /**
                     * text : 已帮您将冰箱设置为智能模式
                     */

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
