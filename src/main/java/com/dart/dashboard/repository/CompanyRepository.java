package com.dart.dashboard.repository;

import com.dart.dashboard.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // DB랑 통신하는 작업자(창구)
public interface CompanyRepository extends JpaRepository<Company, String> {
    
    /*이 안에는 아무런 코드를 적지 않아도 상관 없음
    JpaRepository를 상속받는 순간, 기본적으로 필요한 저장(save), 조회(findById), 삭제(delete) 기능이 자동으로 다 만들어짐
    JPA의 ORM기술을 통해 SQL을 직접 작성할 필요 없이 객체 중심으로 DB를 조작하게 하여, 생산성과 유지보수성 높임*/
}