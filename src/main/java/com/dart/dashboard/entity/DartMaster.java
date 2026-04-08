package com.dart.dashboard.entity; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DART_MASTER") // 사전용 별도 테이블
@Getter
@Setter
public class DartMaster {

    @Id
    @Column(name = "corp_code", length = 8)
    private String corpCode; // DART 고유번호 (예: 00126380)

    private String corpName; // 기업명

    @Column(name = "stock_code", length = 6)
    private String stockCode; // 종목코드 (예: 005930)
}