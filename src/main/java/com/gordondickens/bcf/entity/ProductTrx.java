package com.gordondickens.bcf.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

@Entity
public class ProductTrx implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@OneToOne
	private com.gordondickens.bcf.entity.Product product;

	@NotNull
	private String store;

	@NotNull
	private Integer quantity;

	@NotNull
	private BigDecimal price;

	@NotNull
	private DateTime trxDate;

	private String comment;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Version
	@Column(name = "version")
	private Integer version;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getStore() {
		return this.store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public DateTime getTrxDate() {
		return this.trxDate;
	}

	public void setTrxDate(DateTime trxDate) {
		this.trxDate = trxDate;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Comment: ").append(getComment()).append(", ");
		sb.append("Id: ").append(getId()).append(", ");
		sb.append("Price: ").append(getPrice()).append(", ");
		sb.append("Product: ").append(getProduct()).append(", ");
		sb.append("Quantity: ").append(getQuantity()).append(", ");
		sb.append("Store: ").append(getStore()).append(", ");
		sb.append("TrxDate: ").append(getTrxDate()).append(", ");
		sb.append("Version: ").append(getVersion());
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductTrx other = (ProductTrx) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
