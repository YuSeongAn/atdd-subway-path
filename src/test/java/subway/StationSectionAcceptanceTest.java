package subway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import utils.AcceptanceUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@DisplayName("지하철 구간 관리 기능")
@Sql(scripts = "classpath:reset.sql", executionPhase = BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationSectionAcceptanceTest {

    /**
     * Given 지하철 역 A,B,C,D를 생성한다
     * Given 지하철 역 A,B로 거리가 8m인 노선을 생성한다
     * Given 지하철 역 B,C로 구간을 추가한다
     * When 지하철 노선에 A,D 3m인 구간을 추가한다
     * Then 지하철 노선 조회 시 노선의 역 목록이 A,D,B,C순으로 조회된다
     */
    @DisplayName("정상적인 기존 노선의 역 구간 사이에 새로운 구간 추가")
    @Test
    void createStationLineSection_To_Between_Station() {
        //given
        final List<Long> stationIds = AcceptanceUtils.createStations(List.of("A역", "B역", "C역", "D역"));
        final Long aStationId = stationIds.get(0);
        final Long bStationId = stationIds.get(1);
        final Long cStationId = stationIds.get(2);
        final Long dStationId = stationIds.get(3);

        final Long lineId = AcceptanceUtils.createStationLine("1호선", "blue", aStationId, bStationId, BigDecimal.valueOf(8));
        AcceptanceUtils.createStationLineSection(lineId, bStationId, cStationId, BigDecimal.valueOf(5));

        //when
        AcceptanceUtils.createStationLineSection(lineId, aStationId, dStationId, BigDecimal.valueOf(3), HttpStatus.OK);

        //then
        final List<String> stationNames = AcceptanceUtils.getStationLine(lineId).getList("stations.name", String.class);
        Assertions.assertArrayEquals(List.of("A역", "D역", "B역", "C역").toArray(), stationNames.toArray());
    }

    /**
     * Given 지하철 역 A,B,C,D를 생성한다
     * Given 지하철 역 A,B로 노선을 생성한다
     * Given 지하철 역 B,C로 구간을 추가한다
     * When 지하철 노선에 D,A 구간을 추가한다
     * Then 지하철 노선 조회 시 노선의 역 목록이 D,A,B,C순으로 조회된다
     */
    @DisplayName("정상적으로 새로운 역을 기존 노선의 구간의 상행 종점 역으로 구간 추가")
    @Test
    void createStationLineSection_To_FirstUpStation() {
        //given
        final List<Long> stationIds = AcceptanceUtils.createStations(List.of("A역", "B역", "C역", "D역"));
        final Long aStationId = stationIds.get(0);
        final Long bStationId = stationIds.get(1);
        final Long cStationId = stationIds.get(2);
        final Long dStationId = stationIds.get(3);

        final Long lineId = AcceptanceUtils.createStationLine("1호선", "blue", aStationId, bStationId, BigDecimal.TEN);
        AcceptanceUtils.createStationLineSection(lineId, bStationId, cStationId, BigDecimal.TEN);

        //when
        AcceptanceUtils.createStationLineSection(lineId, dStationId, aStationId, BigDecimal.ONE);

        //then
        final List<String> stationNames = AcceptanceUtils.getStationLine(lineId).getList("stations.name", String.class);
        Assertions.assertArrayEquals(List.of("D역", "A역", "B역", "C역").toArray(), stationNames.toArray());
    }

    /**
     * Given 지하철 역 A,B,C,D를 생성한다
     * Given 지하철 역 A,B로 노선을 생성한다
     * Given 지하철 역 B,C로 구간을 추가한다
     * When 지하철 노선에 C,D 구간을 추가한다
     * Then 지하철 노선 조회 시 노선의 역 목록이 A,B,C,D순으로 조회된다
     */
    @DisplayName("정상적으로 새로운 역을 기존 노선의 구간의 하행 종점 역으로 구간 추가")
    @Test
    void createStationLineSection_To_LastDownStation() {
        //given
        final List<Long> stationIds = AcceptanceUtils.createStations(List.of("A역", "B역", "C역", "D역"));
        final Long aStationId = stationIds.get(0);
        final Long bStationId = stationIds.get(1);
        final Long cStationId = stationIds.get(2);
        final Long dStationId = stationIds.get(3);

        final Long lineId = AcceptanceUtils.createStationLine("1호선", "blue", aStationId, bStationId, BigDecimal.TEN);
        AcceptanceUtils.createStationLineSection(lineId, bStationId, cStationId, BigDecimal.TEN);

        //when
        AcceptanceUtils.createStationLineSection(lineId, cStationId, dStationId, BigDecimal.ONE);

        //then
        final List<String> stationNames = AcceptanceUtils.getStationLine(lineId).getList("stations.name", String.class);
        Assertions.assertArrayEquals(List.of("A역", "B역", "C역", "D역").toArray(), stationNames.toArray());
    }

    /**
     * Given 지하철 역 A,B,C를 생성한다
     * Given 지하철 역 A,B로 구간의 길이가 10m인 노선을 생성한다
     * When 지하철 노선에 A,C로 길이가 12m인 구간을 추가한다
     * Then 에러 발생
     */
    @DisplayName("거리가 10m인 기존 노선의 역 구간 사이에 12m로 새로운 구간 추가시 애러")
    @Test
    void create_12M_StationLineSection_To_Between_Station_Has_10M() {
        //given
        final List<Long> stationIds = AcceptanceUtils.createStations(List.of("A역", "B역", "C역"));
        final Long aStationId = stationIds.get(0);
        final Long bStationId = stationIds.get(1);
        final Long cStationId = stationIds.get(2);

        final Long lineId = AcceptanceUtils.createStationLine("1호선", "blue", aStationId, bStationId, BigDecimal.TEN);

        //when & then
        AcceptanceUtils.createStationLineSection(lineId, aStationId, cStationId, BigDecimal.valueOf(12), HttpStatus.BAD_REQUEST);
    }

    /**
     * Given 지하철 역 A,B,C를 생성한다
     * Given A,B로 지하철 노선 생성한다
     * Given B,C로 지하철 구간을 추가한다
     * When 지하철 노선에 A,C로 구간을 추가한다
     * Then 에러 발생
     */
    @DisplayName("구간의 상행역과 하행역이 모두 노선에 등록된 역일 경우 구간 추가 시 애러")
    @Test
    void createStationLineSection_Both_Station_Existing_To_StationLine() {
        //given
        final List<Long> stationIds = AcceptanceUtils.createStations(List.of("A역", "B역", "C역"));
        final Long aStationId = stationIds.get(0);
        final Long bStationId = stationIds.get(1);
        final Long cStationId = stationIds.get(2);

        final Long lineId = AcceptanceUtils.createStationLine("1호선", "blue", aStationId, bStationId, BigDecimal.TEN);
        AcceptanceUtils.createStationLineSection(lineId, bStationId, cStationId, BigDecimal.TEN);

        //when & then
        AcceptanceUtils.createStationLineSection(lineId, aStationId, cStationId, BigDecimal.TEN, HttpStatus.BAD_REQUEST);
    }

    /**
     * Given 지하철 역 A,B,C,D를 생성한다
     * Given A,B로 지하철 노선 생성한다
     * When 지하철 노선에 C,D로 구간을 추가한다
     * Then 에러 발생
     */
    @DisplayName("구간의 상행역과 하행역이 모두 노선에 포함되지 않은 역일 경우 구간 추가시 애러")
    @Test
    void createStationLineSection_Both_Station_NotExisting_To_stationLine() {
        //given
        final List<Long> stationIds = AcceptanceUtils.createStations(List.of("A역", "B역", "C역", "D역"));
        final Long aStationId = stationIds.get(0);
        final Long bStationId = stationIds.get(1);
        final Long cStationId = stationIds.get(2);
        final Long dStationId = stationIds.get(3);

        final Long lineId = AcceptanceUtils.createStationLine("1호선", "blue", aStationId, bStationId, BigDecimal.TEN);

        //when & then
        AcceptanceUtils.createStationLineSection(lineId, cStationId, dStationId, BigDecimal.TEN, HttpStatus.BAD_REQUEST);
    }

}
