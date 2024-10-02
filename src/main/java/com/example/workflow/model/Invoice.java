package com.example.workflow.model;

public class Invoice {
  
    private String id;
  
    public Invoice() {
    }

    public Invoice(String id){
        this.id = id;
    }
    
    public void setId(String id){
        this.id=id;
    }

    public String getId(){
        return this.id;
    }

    @Override
    public String toString() {
      return "Invoice [id=" + id + "]";
    }
  
  }