package com.example.camscan.ObjectClass;
public class GridViewImagesList
{
    private  String image_url,image_date,image_edited_date,name;
    private  int did,pcount;
   private  boolean checkbox_visibility,ischecked;
    public GridViewImagesList(String name,int did,String image_url,String image_date,int pcount,String image_edited_date,boolean
                              checkbox_visibility,boolean ischecked)
    {
        this.did=did;
        this.image_url=image_url;
        this.image_date=image_date;
        this.pcount=pcount;
        this.image_edited_date=image_edited_date;
        this.checkbox_visibility=checkbox_visibility;
        this.ischecked=ischecked;
        this.name=name;
    }
    public  String getImage_url()
    {
        return  image_url;
    }
    public  String getImage_date()
    {
        return  image_date;
    }
    public  String getImage_edited_date()
    {
        return  image_edited_date;
    }
    public  int getDid()
    {
        return  did;
    }
    public  int getPcount()
    {
        return  pcount;
    }
    public boolean checkbox_visibility() {
        return checkbox_visibility;
    }
    public boolean Ischecked() {
        return ischecked;
    }
    public void setCheckbox_visibility(boolean b)
    {
        checkbox_visibility=b;
    }
    public  void setIschecked(boolean b)
    {
        ischecked=b;
    }

    public String getName() {
        return name;
    }
}
