package io.intelliflow.repomanager.model;

import java.util.List;

public class CloneInfo {

        private List<String> bpmn;
        private List<String> forms;
        private List<String> dmn;
        private List<String> datamodel;

        public List<String> getBpmn() {
            return bpmn;
        }

        public void setBpmn(List<String> bpmn) {
            this.bpmn = bpmn;
        }

        public List<String> getForms() {
            return forms;
        }

        public void setForms(List<String> forms) {
            this.forms = forms;
        }

        public List<String> getDmn() {
            return dmn;
        }

        public void setDmn(List<String> dmn) {
            this.dmn = dmn;
        }

        public List<String> getDatamodel() {
            return datamodel;
        }

        public void setDatamodel(List<String> datamodel) {
            this.datamodel = datamodel;
        }
    }


