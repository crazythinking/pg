/**
 * 
 */
package net.engining.pg.support.db.audited;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import net.engining.pg.support.meta.PropertyInfo;

/**
 * 审计字段 超类，生成Entity需要继承该超类
 * @author luxue
 *
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractAuditingEntity {
	
	@Column(name = "CREATED_USER", nullable = false)
    @CreatedBy
    private String createdUser;

	@PropertyInfo(name="\u521B\u5EFA\u65E5\u671F")
    @Temporal(value=TemporalType.TIMESTAMP)
    @Column(name="SETUP_DATE", nullable=false)
	@CreatedDate
    private Date setupDate;
	
	@Column(name = "MODIFIED_USER", nullable = false)
    @LastModifiedBy
    private String modifiedUser;

    @PropertyInfo(name="\u6700\u540E\u66F4\u65B0\u65E5\u671F")
    @Temporal(value=TemporalType.TIMESTAMP)
    @Column(name="LAST_UPDATE_DATE", nullable=true)
    @LastModifiedDate
    private Date lastUpdateDate;
}
