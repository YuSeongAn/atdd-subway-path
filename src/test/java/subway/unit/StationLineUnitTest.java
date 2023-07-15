package subway.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import subway.domain.Station;
import subway.domain.StationLine;
import subway.domain.StationLineSection;
import subway.exception.StationLineCreateException;
import subway.exception.StationLineSectionDeleteException;
import subway.exception.StationLineSectionSplitException;
import utils.AcceptanceUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static utils.UnitTestUtils.createEntityTestId;
import static utils.UnitTestUtils.createEntityTestIds;

public class StationLineUnitTest {

    @DisplayName("정상적인 노선의 역 사이에 구간의 추가")
    @Test
    void createStationLineSection_newStation_Between_LineStations() {
        //given
        final Station lineUpStation = new Station("lineUpStation");
        final Station lineDownStation = new Station("lineDownStation");
        final Station sectionDownStation = new Station("newStation");

        createEntityTestIds(List.of(lineUpStation, lineDownStation, sectionDownStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(lineUpStation)
                .downStation(lineDownStation)
                .distance(BigDecimal.TEN)
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.createSection(lineUpStation, sectionDownStation, BigDecimal.ONE));

        //then
        final List<Station> expectedLineStations = List.of(lineUpStation, sectionDownStation, lineDownStation);
        final List<BigDecimal> lineSectionDistances = line.getSections()
                .stream()
                .map(StationLineSection::getDistance)
                .collect(Collectors.toList());

        Assertions.assertArrayEquals(expectedLineStations.toArray(), line.getAllStations().toArray());

        Assertions.assertEquals(BigDecimal.ONE, lineSectionDistances.get(0));
        Assertions.assertEquals(BigDecimal.valueOf(9), lineSectionDistances.get(1));
    }

    @DisplayName("새로운 구간의 길이가 기존 노선의 역 구간 거리보다 크거나 같은 경우 애러")
    @Test
    void createStationLineSection_newStation_Between_LineStations_largeThan_Exising_Distance() {
        //given
        final Station lineUpStation = new Station("lineUpStation");
        final Station lineDownStation = new Station("lineDownStation");
        final Station sectionDownStation = new Station("newStation");

        createEntityTestIds(List.of(lineUpStation, lineDownStation, sectionDownStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(lineUpStation)
                .downStation(lineDownStation)
                .distance(BigDecimal.valueOf(5L))
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        final Throwable throwable = Assertions.assertThrows(StationLineSectionSplitException.class,
                () -> line.createSection(lineUpStation, sectionDownStation, BigDecimal.TEN));

        Assertions.assertEquals("can't split existing section into larger distance section", throwable.getMessage());
    }

    @DisplayName("정상적인 구간의 새로운 역이 노선의 상행종점역인 구간 추가")
    @Test
    void createStationLineSection_newStation_Equals_To_Line_FirstUpStation() {
        //given
        final Station lineUpStation = new Station("lineUpStation");
        final Station lineDownStation = new Station("lineDownStation");
        final Station sectionUpStation = new Station("newStation");

        createEntityTestIds(List.of(lineUpStation, lineDownStation, sectionUpStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(lineUpStation)
                .downStation(lineDownStation)
                .distance(BigDecimal.ONE)
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.createSection(sectionUpStation, lineUpStation, BigDecimal.TEN));

        //then
        final List<Station> expectedLineStations = List.of(sectionUpStation, lineUpStation, lineDownStation);
        final List<BigDecimal> lineSectionDistances = line.getSections()
                .stream()
                .map(StationLineSection::getDistance)
                .collect(Collectors.toList());

        Assertions.assertArrayEquals(expectedLineStations.toArray(), line.getAllStations().toArray());

        Assertions.assertEquals(BigDecimal.TEN, lineSectionDistances.get(0));
        Assertions.assertEquals(BigDecimal.ONE, lineSectionDistances.get(1));
    }

    @DisplayName("정상적인 구간의 새로운 역이 노선의 하행종점역인 구간 추가")
    @Test
    void createStationLineSection_newStation_Equals_To_Line_LastDownStation() {
        //given
        final Station lineUpStation = new Station("lineUpStation");
        final Station lineDownStation = new Station("lineDownStation");
        final Station sectionDownStation = new Station("newStation");

        createEntityTestIds(List.of(lineUpStation, lineDownStation, sectionDownStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(lineUpStation)
                .downStation(lineDownStation)
                .distance(BigDecimal.ONE)
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.createSection(lineDownStation, sectionDownStation, BigDecimal.TEN));

        //then
        final List<Station> expectedLineStations = List.of(lineUpStation, lineDownStation, sectionDownStation);
        final List<BigDecimal> lineSectionDistances = line.getSections()
                .stream()
                .map(StationLineSection::getDistance)
                .collect(Collectors.toList());

        Assertions.assertArrayEquals(expectedLineStations.toArray(), line.getAllStations().toArray());

        Assertions.assertEquals(BigDecimal.ONE, lineSectionDistances.get(0));
        Assertions.assertEquals(BigDecimal.TEN, lineSectionDistances.get(1));
    }

    @DisplayName("추가하려는 구간의 역이 노선에 하나도 포함이 안된 경우 예외 발생")
    @Test
    void createStationLineSection_Both_sectionStation_Not_Included_To_Line() {
        //given
        final Station lineUpStation = new Station("lineUpStation");
        final Station lineDownStation = new Station("lineDownStation");
        final Station sectionUpStation = new Station("sectionUpStation");
        final Station sectionDownStation = new Station("sectionDownStation");

        createEntityTestIds(List.of(lineUpStation, lineDownStation, sectionUpStation, sectionDownStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(lineUpStation)
                .downStation(lineDownStation)
                .distance(BigDecimal.ONE)
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when & then
        final Throwable throwable = Assertions.assertThrows(StationLineCreateException.class,
                () -> line.createSection(sectionUpStation, sectionDownStation, BigDecimal.TEN));

        Assertions.assertEquals("one of section up station and down station exactly exist only one to line", throwable.getMessage());
    }

    @DisplayName("추가하려는 구간의 역이 모두 노선에 존재하는 경우 예외 발생")
    @Test
    void createStationLineSection_Both_sectionStation_Included_To_Line() {
        //given
        final Station lineUpStation = new Station("lineUpStation");
        final Station lineDownStation = new Station("lineDownStation");

        createEntityTestIds(List.of(lineUpStation, lineDownStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(lineUpStation)
                .downStation(lineDownStation)
                .distance(BigDecimal.ONE)
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when & then
        final Throwable throwable = Assertions.assertThrows(StationLineCreateException.class,
                () -> line.createSection(lineUpStation, lineDownStation, BigDecimal.TEN));

        Assertions.assertEquals("one of section up station and down station exactly exist only one to line", throwable.getMessage());
    }

    @DisplayName("정상적인 노선의 마지막 역 삭제")
    @Test
    void deleteStationLineSection_lastStation() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");
        final Station cStation = new Station("cStation");

        createEntityTestIds(List.of(aStation, bStation, cStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();

        line.createSection(bStation, cStation, BigDecimal.ONE);

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.deleteSection(cStation));

        //then
        final Station lineLastDownStation = line.getLineLastStation();

        Assertions.assertEquals(bStation, lineLastDownStation);
        Assertions.assertFalse(line.getAllStations().contains(cStation));
    }

    @DisplayName("정상적인 노선의 첫번째 역 삭제")
    @Test
    void deleteStationLineSection_firstStation() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");
        final Station cStation = new Station("cStation");

        createEntityTestIds(List.of(aStation, bStation, cStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();

        line.createSection(bStation, cStation, BigDecimal.ONE);

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.deleteSection(aStation));

        //then
        final Station lineFirstStation = line.getLineFirstStation();

        Assertions.assertEquals(bStation, lineFirstStation);
        Assertions.assertFalse(line.getAllStations().contains(aStation));
    }

    @DisplayName("정상적인 노선의 중간역 삭제")
    @Test
    void deleteStationLineSection_middleStation() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");
        final Station cStation = new Station("cStation");

        createEntityTestIds(List.of(aStation, bStation, cStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();

        line.createSection(bStation, cStation, BigDecimal.ONE);

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.deleteSection(bStation));

        //then
        final Station lineFirstStation = line.getLineFirstStation();
        final Station lineLastStation = line.getLineLastStation();

        Assertions.assertEquals(aStation, lineFirstStation);
        Assertions.assertEquals(cStation, lineLastStation);
        Assertions.assertFalse(line.getAllStations().contains(bStation));

        final BigDecimal expectedNewSectionDistance = BigDecimal.valueOf(11);
        final BigDecimal sectionDistance = line.getSections().get(0).getDistance();
        Assertions.assertEquals(0, sectionDistance.compareTo(expectedNewSectionDistance));
    }

    @DisplayName("정상적인 노선의 상행종점역 삭제")
    @Test
    void deleteStationLineSection_FirstStation() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");
        final Station cStation = new Station("cStation");

        createEntityTestIds(List.of(aStation, bStation, cStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();

        line.createSection(bStation, cStation, BigDecimal.ONE);

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.deleteSection(aStation));

        //then
        final Station lineFirstStation = line.getLineFirstStation();
        final Station lineLastStation = line.getLineLastStation();

        Assertions.assertEquals(bStation, lineFirstStation);
        Assertions.assertEquals(cStation, lineLastStation);
        Assertions.assertFalse(line.getAllStations().contains(aStation));
    }

    @DisplayName("정상적인 노선의 하행종점역 삭제")
    @Test
    void deleteStationLineSection_LastStation() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");
        final Station cStation = new Station("cStation");

        createEntityTestIds(List.of(aStation, bStation, cStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();

        line.createSection(bStation, cStation, BigDecimal.ONE);

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when
        Assertions.assertDoesNotThrow(() -> line.deleteSection(cStation));

        //then
        final Station lineFirstStation = line.getLineFirstStation();
        final Station lineLastStation = line.getLineLastStation();

        Assertions.assertEquals(aStation, lineFirstStation);
        Assertions.assertEquals(bStation, lineLastStation);
        Assertions.assertFalse(line.getAllStations().contains(cStation));
    }


    @DisplayName("삭제하려는 역의 지하철 노선이 2개의 역만 가진 노선일 경우 예외")
    @Test
    void deleteStationLineSection_has_2_StationLine() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");

        createEntityTestIds(List.of(aStation, bStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();

        createEntityTestId(line, 1L);
        createEntityTestIds(line.getSections(), 1L);

        //when & then
        final Throwable throwable = Assertions.assertThrows(StationLineSectionDeleteException.class,
                () -> line.deleteSection(bStation));

        Assertions.assertEquals("section must be greater or equals than 2", throwable.getMessage());
    }

    @DisplayName("삭제하려는 역이 노선에 존재하지 않을 경우 예외")
    @Test
    void deleteStationLineSection_Not_Included_In_StationLine() {
        //given
        final Station aStation = new Station("aStation");
        final Station bStation = new Station("bStation");
        final Station cStation = new Station("cStation");
        final Station dStation = new Station("dStation");

        createEntityTestIds(List.of(aStation, bStation, cStation, dStation), 1L);

        final StationLine line = StationLine.builder()
                .name("1호선")
                .color("blue")
                .upStation(aStation)
                .downStation(bStation)
                .distance(BigDecimal.TEN)
                .build();
        createEntityTestId(line, 1L);

        line.createSection(bStation,cStation,BigDecimal.ONE);
        createEntityTestIds(line.getSections(), 1L);

        //when & then
        final Throwable throwable = Assertions.assertThrows(StationLineSectionDeleteException.class,
                () -> line.deleteSection(dStation));

        Assertions.assertEquals("the station not exists to this line", throwable.getMessage());
    }
}
