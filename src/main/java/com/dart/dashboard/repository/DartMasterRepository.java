package com.dart.dashboard.repository;

import com.dart.dashboard.entity.DartMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DartMasterRepository extends JpaRepository<DartMaster, String> {
    // 나중에 종목코드(005930)를 넣으면 DART 고유번호를 찾아주는 메서드
    Optional<DartMaster> findByStockCode(String stockCode);
    // 기업명으로 고유번호 찾기
    Optional<DartMaster> findByCorpName(String corpName);
}