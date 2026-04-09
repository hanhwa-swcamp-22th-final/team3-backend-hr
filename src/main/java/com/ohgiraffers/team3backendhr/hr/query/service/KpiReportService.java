package com.ohgiraffers.team3backendhr.hr.query.service;

import com.ohgiraffers.team3backendhr.common.exception.BusinessException;
import com.ohgiraffers.team3backendhr.common.exception.ErrorCode;
import com.ohgiraffers.team3backendhr.hr.query.dto.response.dashboard.HrmKpiDetailItem;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiReportService {

    private final DashboardQueryService dashboardQueryService;

    public byte[] generateHrmKpiExcel(int year, int quarter) {
        List<HrmKpiDetailItem> items = dashboardQueryService.getHrmKpiDetailsAll(year, quarter);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("KPI Report " + year + " Q" + quarter);

            // 1. 헤더 스타일 생성 (굵은 글씨 + 배경색)
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 2. 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] columns = {"사원ID", "성명", "현재티어", "정량점수", "최종등급", "평가상태"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. 데이터 행 생성
            int rowIdx = 1;
            for (HrmKpiDetailItem item : items) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getEmployeeId());
                row.createCell(1).setCellValue(item.getEmployeeName());
                row.createCell(2).setCellValue(item.getEmployeeTier());
                
                // 개선: Null 안전 처리 (평가 전인 경우 0.0 처리)
                Double score = item.getQualitativeScore();
                row.createCell(3).setCellValue(score != null ? score : 0.0);
                
                row.createCell(4).setCellValue(item.getGrade() != null ? item.getGrade() : "-");
                row.createCell(5).setCellValue(item.getEvalStatus());
            }

            // 4. 컬럼 너비 자동 조정
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "엑셀 파일 생성 중 오류가 발생했습니다.");
        }
    }
}
