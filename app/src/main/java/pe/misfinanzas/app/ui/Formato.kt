package pe.misfinanzas.app.ui

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val LOCALE_PE: Locale = Locale("es", "PE")

fun soles(v: Double): String = "S/ " + String.format(LOCALE_PE, "%,.2f", v)
fun solesCorto(v: Double): String = "S/ " + String.format(LOCALE_PE, "%,.0f", v)

private fun String.capitalizar() = replaceFirstChar { it.titlecase(LOCALE_PE) }

fun mesTitulo(m: YearMonth): String =
    m.format(DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_PE)).capitalizar()

fun mesCorto(m: YearMonth): String =
    m.format(DateTimeFormatter.ofPattern("MMM", LOCALE_PE)).replace(".", "").capitalizar()

fun fechaDia(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LOCALE_PE)).capitalizar()

fun fechaCorta(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("d MMM", LOCALE_PE)).replace(".", "")

fun fechaBoton(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("EEE d 'de' MMMM yyyy", LOCALE_PE)).capitalizar()
