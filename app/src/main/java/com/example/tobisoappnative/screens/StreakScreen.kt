package com.example.tobisoappnative.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tobisoappnative.cancelStreakNotifications
import com.example.tobisoappnative.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    // Stav pro aktuální měsíc a rok
    val today = remember { Calendar.getInstance() }
    var calendarMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var calendarYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }

    // Přidá dnešní den do streaku při každém otevření obrazovky
    LaunchedEffect(Unit) {
        addTodayToStreak(context)
    }

    // Použití remember s key pro refresh při změně měsíce/roku
    val streakDays by remember(calendarMonth, calendarYear) {
        mutableStateOf(getStreakDays(context))
    }
    val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
    val currentStreak = remember(streakDays) { getCurrentStreak(streakDays) }
    val maxStreak = remember(streakDays) { getMaxStreak(streakDays) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LargeTopAppBar(
            title = { Text("Řada") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Zpět")
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Zobrazení maximálního a aktuálního streaku s ikonou ohně ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Whatshot,
                            contentDescription = "Řada",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Aktuální řada",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "$currentStreak ${denDnyDni(currentStreak)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Whatshot,
                            contentDescription = "Max Streak",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Nejdelší řada",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "$maxStreak ${denDnyDni(maxStreak)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Navigace měsíce
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (calendarMonth == 0) {
                        calendarMonth = 11
                        calendarYear -= 1
                    } else {
                        calendarMonth -= 1
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Předchozí měsíc")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = monthYearString(calendarMonth, calendarYear),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (calendarMonth == 11) {
                        calendarMonth = 0
                        calendarYear += 1
                    } else {
                        calendarMonth += 1
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Další měsíc")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CalendarStreak(
                streakDays = streakDays,
                month = calendarMonth,
                year = calendarYear,
                todayString = currentDateString
            )
        }
    }
}

@Composable
fun CalendarStreak(
    streakDays: Set<String>,
    month: Int,
    year: Int,
    todayString: String
) {
    val days = getMonthDaysGrid(month, year)
    val weekDays = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        weekDays.forEach { weekDay ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weekDay,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Column {
        for (week in days.chunked(7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                for (day in week) {
                    val isActive = day.fullDate != null && streakDays.contains(day.fullDate)
                    val isToday = day.fullDate == todayString
                    val isCurrentMonth = day.isCurrentMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .then(
                                if (isToday) Modifier else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isToday) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primary,
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = day.dayNumber.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = when {
                                            isActive -> MaterialTheme.colorScheme.secondary
                                            isCurrentMonth -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayNumber.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isCurrentMonth) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

data class CalendarDay(val dayNumber: Int, val isCurrentMonth: Boolean, val fullDate: String?)

fun getMonthDaysGrid(month: Int, year: Int): List<CalendarDay> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=Po, 6=Ne
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Předchozí měsíc
    val prevMonth = if (month == 0) 11 else month - 1
    val prevYear = if (month == 0) year - 1 else year
    val prevCalendar = Calendar.getInstance()
    prevCalendar.set(Calendar.YEAR, prevYear)
    prevCalendar.set(Calendar.MONTH, prevMonth)
    val daysInPrevMonth = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Začátek gridu
    val days = mutableListOf<CalendarDay>()

    // Dny z předchozího měsíce
    for (i in 0 until firstDayOfWeek) {
        val dayNum = daysInPrevMonth - firstDayOfWeek + i + 1
        val date = getDateString(prevYear, prevMonth, dayNum)
        days.add(CalendarDay(dayNum, false, date))
    }

    // Aktuální měsíc
    for (i in 1..daysInMonth) {
        val date = getDateString(year, month, i)
        days.add(CalendarDay(i, true, date))
    }

    // Další měsíc
    val nextMonth = if (month == 11) 0 else month + 1
    val nextYear = if (month == 11) year + 1 else year
    var nextDay = 1
    while (days.size % 7 != 0) {
        val date = getDateString(nextYear, nextMonth, nextDay)
        days.add(CalendarDay(nextDay, false, date))
        nextDay++
    }

    return days
}

fun getDateString(year: Int, month: Int, day: Int): String {
    return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
}

fun monthYearString(month: Int, year: Int): String {
    val months = listOf(
        "Leden", "Únor", "Březen", "Duben", "Květen", "Červen",
        "Červenec", "Srpen", "Září", "Říjen", "Listopad", "Prosinec"
    )
    return "${months[month]} $year"
}

fun getCurrentStreak(streakDays: Set<String>): Int {
    if (streakDays.isEmpty()) return 0

    val sortedDays = streakDays.sortedDescending()
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val calendar = Calendar.getInstance()

    var currentStreak = 0
    var checkDate = today

    while (sortedDays.contains(checkDate)) {
        currentStreak++
        calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(checkDate) ?: break
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        checkDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    return currentStreak
}

fun getMaxStreak(streakDays: Set<String>): Int {
    if (streakDays.isEmpty()) return 0

    val sortedDays = streakDays.sorted()
    var maxStreak = 1
    var currentStreak = 1

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    for (i in 1 until sortedDays.size) {
        val prevDate = dateFormat.parse(sortedDays[i - 1])
        val currentDate = dateFormat.parse(sortedDays[i])

        if (prevDate != null && currentDate != null) {
            calendar.time = prevDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            if (calendar.time.equals(currentDate)) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
    }

    return maxStreak
}

fun getLast30Days(): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    return (0 until 30).map { i ->
        val date = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        date
    }.reversed()
}

fun getStreakDays(context: Context): Set<String> {
    val prefs = context.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
    return prefs.getStringSet("days", emptySet()) ?: emptySet()
}

fun addTodayToStreak(context: Context) {
    val prefs = context.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val existingDays = prefs.getStringSet("days", emptySet()) ?: emptySet()

    if (!existingDays.contains(today)) {
        val days = existingDays.toMutableSet()
        days.add(today)
        prefs.edit().putStringSet("days", days).apply()
        cancelStreakNotifications(context)
    }
}

// --- Pomocná funkce pro české skloňování slova "den" ---
fun denDnyDni(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        count == 1 -> "den"
        (mod10 in 2..4) && !(mod100 in 12..14) -> "dny"
        else -> "dní"
    }
}
