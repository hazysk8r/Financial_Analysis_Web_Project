package com.dart.dashboard.service; 

import com.dart.dashboard.entity.DartMaster;
import com.dart.dashboard.repository.DartMasterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
public class DartMasterService {

    private final DartMasterRepository dartMasterRepository;
    private final String apiKey;

    public DartMasterService(DartMasterRepository dartMasterRepository,
                             @Value("${dart.api-key}") String apiKey) {
        this.dartMasterRepository = dartMasterRepository;
        this.apiKey = apiKey;
    }

    // DART 서버에서 10만 개 기업 리스트(ZIP)를 다운받아 DB에 저장하는 로직
    public void downloadAndSaveMasterData() {
        String url = "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();

        System.out.println(" DART에서 ZIP 파일 다운로드 중...");

        try {
            // 1. ZIP 파일을 통째로 메모리에 다운로드 (byte 배열 형태)
            byte[] zipBytes = restTemplate.getForObject(url, byte[].class);

            // 2. 압축 풀기 준비!
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
                zis.getNextEntry(); // 압축 파일 안의 첫 번째 파일(CORPCODE.xml) 오픈

                // 3. XML 파일 분석(파싱) 준비
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(zis);
                NodeList list = document.getElementsByTagName("list");

                List<DartMaster> masters = new ArrayList<>();

                System.out.println("XML 파일 분석 및 상장사 필터링 중...");

                // 4. 10만 개의 기업 목록을 하나씩 돌면서 확인
                for (int i = 0; i < list.getLength(); i++) {
                    Element element = (Element) list.item(i);
                    String corpCode = element.getElementsByTagName("corp_code").item(0).getTextContent();
                    String corpName = element.getElementsByTagName("corp_name").item(0).getTextContent();
                    String stockCode = element.getElementsByTagName("stock_code").item(0).getTextContent().trim();

                    // 최적화: 종목코드(stockCode)가 비어있지 않은 '상장사'만 저장
                    if (!stockCode.isEmpty()) {
                        DartMaster master = new DartMaster();
                        master.setCorpCode(corpCode);
                        master.setCorpName(corpName);
                        master.setStockCode(stockCode);
                        masters.add(master);
                    }
                }

                // 5. DB에 한 번에 쏟아붓기 (saveAll을 사용해서 속도 up)
                dartMasterRepository.deleteAll(); // 혹시 기존 데이터가 있으면 지우고
                dartMasterRepository.saveAll(masters); // 최신 데이터로 덮어쓰기

                System.out.println("[마스터 사전] " + masters.size() + "개의 상장사 마스터 데이터 DB 저장 완료!");
            }
        } catch (Exception e) {
            System.out.println("마스터 데이터 세팅 중 에러 발생: " + e.getMessage());
        }
    }
}