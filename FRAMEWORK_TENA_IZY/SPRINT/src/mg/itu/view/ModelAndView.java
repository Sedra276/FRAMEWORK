package mg.itu.view;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String view;

    private Map<String, Object> data;

    public ModelAndView() {
        this.data = new HashMap<>();
    }

    public ModelAndView(String view) {
        this.view = view;
        this.data = new HashMap<>();
    }

    public void addObject(String nom, Object obj) {
        data.put(nom, obj);
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {

        return "ModelAndView{view='"
                + view
                + "', data="
                + data
                + "}";

    }

}
