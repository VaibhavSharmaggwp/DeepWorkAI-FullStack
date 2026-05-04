package com.example.plugins

import com.example.db.DatabaseFactory
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.*

import com.example.security.JwtService

fun Application.configureExportRoutes(jwtService: JwtService) {
    routing {
        route("/api/export") {
            // Helper to get principal from header or query param
            suspend fun ApplicationCall.getAuthenticatedPrincipal(): JWTPrincipal? {
                val headerPrincipal = principal<JWTPrincipal>()
                if (headerPrincipal != null) return headerPrincipal
                
                val token = request.queryParameters["token"] ?: return null
                return try {
                    val decoded = jwtService.verifier.verify(token)
                    JWTPrincipal(decoded)
                } catch (e: Exception) {
                    null
                }
            }

            get("/pdf") {
                val principal = call.getAuthenticatedPrincipal()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized or missing user ID")
                    return@get
                }

                val sessions = DatabaseFactory.getWeeklySessions(UUID.fromString(userIdStr))
                
                val outputStream = ByteArrayOutputStream()
                val document = Document(PageSize.A4)
                PdfWriter.getInstance(document, outputStream)
                
                document.open()
                
                // Title
                val fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f)
                val title = Paragraph("DeepWorkAI Weekly Session Report", fontTitle)
                title.alignment = Element.ALIGN_CENTER
                document.add(title)
                document.add(Paragraph("Generated on: ${java.time.LocalDateTime.now()}"))
                document.add(Paragraph("\n"))
                
                // Table
                val table = PdfPTable(4)
                table.widthPercentage = 100f
                table.addCell(PdfPCell(Phrase("Start Time")))
                table.addCell(PdfPCell(Phrase("Focus Score")))
                table.addCell(PdfPCell(Phrase("Distractions")))
                table.addCell(PdfPCell(Phrase("Cognitive Load")))
                
                for (session in sessions) {
                    table.addCell(session.startTime)
                    table.addCell(session.focusScore.toString())
                    table.addCell(session.distractions.toString())
                    table.addCell(session.cognitiveLoad ?: "N/A")
                }
                
                document.add(table)
                document.close()
                
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "weekly_report.pdf").toString()
                )
                call.respondBytes(outputStream.toByteArray(), ContentType.Application.Pdf)
            }

            get("/csv") {
                val principal = call.getAuthenticatedPrincipal()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized or missing user ID")
                    return@get
                }

                val sessions = DatabaseFactory.getWeeklySessions(UUID.fromString(userIdStr))
                
                val outputStream = ByteArrayOutputStream()
                val writer = PrintWriter(outputStream)
                val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Session ID", "Start Time", "End Time", "Focus Score", "Distractions", "Cognitive Load"))
                
                for (session in sessions) {
                    csvPrinter.printRecord(
                        session.id,
                        session.startTime,
                        session.endTime,
                        session.focusScore,
                        session.distractions,
                        session.cognitiveLoad
                    )
                }
                
                csvPrinter.flush()
                writer.flush()
                
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "weekly_data.csv").toString()
                )
                call.respondBytes(outputStream.toByteArray(), ContentType.Text.CSV)
            }
        }
    }
}
