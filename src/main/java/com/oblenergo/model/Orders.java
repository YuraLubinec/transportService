
package com.oblenergo.model;

import java.io.Serializable;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 7191453677142422700L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "name_servise_id")
    private WorkType workType;
    
    @Column(name = "customer")
    private String customer;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "car_name_id")
    private Car car;
    
    @Column(name = "car_model")
    private String car_model;

    
    @Column(name = "sum_vithput_pdv")
    private Double sum_vithput_pdv;

    
    @Column(name = "pdv")
    private Double pdv;

    
    @Column(name = "all_sum")
    private Double all_sum;

    
    @Column(name = "performer_id")
    private Integer performer_id;

   
    @Column(name = "date")
    private String date;

    
    @Column(name = "time")
    private String time;

    @Column(name = "user_tab")
    private Integer user_tab;

    @Column(name = "car_number")
    private String car_number;

    @Column(name = "status_order")
    private Integer status_order;
    
    public Integer getUser_tab() {
        return user_tab;
    }

    public void setUser_tab(Integer user_tab) {
        this.user_tab = user_tab;
    }

    public String getCar_number() {
        return car_number;
    }

    public void setCar_number(String car_number) {
        this.car_number = car_number;
    }

    public Integer getStatus_order() {
        return status_order;
    }

    public void setStatus_order(Integer status_order) {
        this.status_order = status_order;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }
    
    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getCar_model() {
        return car_model;
    }

    public void setCar_model(String car_model) {
        this.car_model = car_model;
    }

    public Double getSum_vithput_pdv() {
        return sum_vithput_pdv;
    }

    public void setSum_vithput_pdv(Double sum_vithput_pdv) {
        this.sum_vithput_pdv = sum_vithput_pdv;
    }

    public Double getPdv() {
        return pdv;
    }

    public void setPdv(Double pdv) {
        this.pdv = pdv;
    }

    public Double getAll_sum() {
        return all_sum;
    }

    public void setAll_sum(Double all_sum) {
        this.all_sum = all_sum;
    }

    public Integer getPerformer_id() {
        return performer_id;
    }

    public void setPerformer_id(Integer performer_id) {
        this.performer_id = performer_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    public WorkType getWorkType(){
        return workType;
    }
    
    public void setWorkType(WorkType workType){
        this.workType = workType;
    }
    
    

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this, true);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj, true);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
