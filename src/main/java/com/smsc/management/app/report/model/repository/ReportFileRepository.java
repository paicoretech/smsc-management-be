package com.smsc.management.app.report.model.repository;

import com.smsc.management.app.report.dto.ReportFileDTO;
import com.smsc.management.app.report.model.entity.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportFileRepository extends JpaRepository<ReportFile, Integer> {
    ReportFile findById(int id);

    @Query("select new com.smsc.management.app.report.dto.ReportFileDTO(" +
            "r.id, r.filename, r.status, r.type, r.extension, r.token, " +
            "r.path, r.createdById, r.user.userName, r.createdAt, r.updatedAt) " +
            "from ReportFile r " +
            "where r.type in :types " +
            "ORDER BY r.id DESC")
    List<ReportFileDTO> findAllByOrderByIdDesc(@Param("types") List<String> types);
}
