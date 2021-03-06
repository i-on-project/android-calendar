package org.ionproject.jdcalendar

import android.content.Context
import java.util.*
import kotlin.math.abs

/**
 * Contains a set of auxiliary functions and properties to facilitate the use of the Calendar type from Java.
 * Instead of affecting an instance of calendar, they create a new one which has been altered.
 * This approach does decrease the performance of the code if in single-threaded environment, but
 * facilitates concurrent access if in a multi-threaded environment.
 * It also provides much better legibility.
 */

/** Returns the current year at which this instance is at */
val Calendar.year get() = get(Calendar.YEAR)

/** Returns the current month at which this instance is at
 * Adds 1 to get(Calendar.MONTH) because it returns the month starting
 * at 0 and we want to start at 1
 * */
val Calendar.month get() = get(Calendar.MONTH) + 1

/** Returns the current week at which this instance is at */
val Calendar.week get() = get(Calendar.WEEK_OF_MONTH)

/** Returns the current day of month at which this instance is at */
val Calendar.day get() = get(Calendar.DAY_OF_MONTH)

/** Returns the current day of week at which this instance is at */
val Calendar.dayOfWeek get() = get(Calendar.DAY_OF_WEEK)

/** Returns the current hour at which this instance is at */
val Calendar.hour get() = get(Calendar.HOUR_OF_DAY)

/** Returns the current minute at which this instance is at */
val Calendar.minute get() = get(Calendar.MINUTE)

/** Returns the current second at which this instance is at */
val Calendar.second get() = get(Calendar.SECOND)

/** Returns true if the current day at which this instance is at is today*/
val Calendar.isToday: Boolean
    get() {
        val today = Calendar.getInstance()
        return day == today.day && month == today.month && year == today.year
    }


const val NUMBER_OF_WEEK_DAYS = 7

fun Calendar.fromMilis(milis: Long): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milis
    return calendar
}


/**
 * Returns a new calendar instance which
 * has been advanced N days from the day
 * of the instance it was called from
 */
fun Calendar.daysFromNow(days: Int): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this.time
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return calendar
}

/**
 * Returns a new calendar instance which
 * has been advanced N months from the month
 * of the instance it was called from
 */
fun Calendar.monthsFromNow(months: Int): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this.time
    calendar.add(Calendar.MONTH, months)
    return calendar
}

/**
 * Returns a new calendar instance which
 * has been set to the first day of the
 * month
 */
fun Calendar.firstDayOfMonth(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this.time
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    return calendar
}

/**
 * Returns last day of the month at which this instance is at
 */
val Calendar.lastDayOfMonth: Int
    get() {
        val calendar = Calendar.getInstance()
        calendar.time = this.time
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.day
    }

/**
 * Updates [daysList] with a new list containing the days of the current
 * month represented in the calendar
 *
 * Logic:
 * Check on which week day the month starts (e.g tuesday,friday)
 * Obtain previous month days (e.g 29,30)
 * Obtain current month days (e.g 01,02,03,04,05,06...31)
 * Obtain next month days (e.g 1,2)
 */
fun Calendar.getDaysOfMonth(): List<Day> {
    var movedCalendar = this.firstDayOfMonth()

    val currMonth = movedCalendar.month
    //Get day of week on which the month starts
    val weekDay = movedCalendar.dayOfWeek

    val numbOfDays =
        if (weekDay == Calendar.SATURDAY && movedCalendar.lastDayOfMonth == 31 || weekDay == Calendar.SUNDAY)
            6 * NUMBER_OF_WEEK_DAYS
        else
            5 * NUMBER_OF_WEEK_DAYS

    //If the week day is sunday, its value is 1 because in USA the weeks start in sunday
    //therefore we have to add 5 instead of subtracting 2
    movedCalendar = if (weekDay == Calendar.SUNDAY)
        movedCalendar.daysFromNow(-(weekDay + 5))
    else
        movedCalendar.daysFromNow(-(weekDay - 2))

    // Get today's Date, in order to check if each day is after today
    val today = Calendar.getInstance()

    return (0 until numbOfDays).map {
        val calendar = movedCalendar.daysFromNow(it)
        Day(
            value = calendar,
            isDayOfCurrMonth = calendar.month == currMonth,
            isToday = calendar.isToday,
            isAfterToday = calendar.after(today)
        )

    }
}

/** Returns the number of time the current week day occurs until endDate */
fun Calendar.weekDaysUntil(endDate: Calendar): Int {
    var count = 0
    val curr = Calendar.getInstance()
    if (curr.dayOfWeek != this.dayOfWeek) {
        curr.add(Calendar.WEEK_OF_MONTH, 1)
        curr.set(Calendar.DAY_OF_WEEK, this.dayOfWeek)
    }
    while (curr.before(endDate)) {
        ++count
        curr.add(Calendar.WEEK_OF_MONTH, 1)
    }
    return count
}

operator fun Calendar.minus(other: Calendar): Calendar {
    val start = other.timeInMillis
    val end = timeInMillis
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = abs(end - start)
    calendar.timeZone = TimeZone.getTimeZone("GMT")
    return calendar
}

/** Return the name of the month at which this instance is at */
fun Calendar.getMonthName(ctx: Context) = Month.values()[month - 1].getName(ctx)

/**
 * Represents a Month and associated with each month is the resource from Strings.xml
 * this way, when the application language is altered, so is the month
 */
private enum class Month(private val monthResId: Int) {
    JANUARY(R.string.january),
    FEBRUARY(R.string.february),
    MARCH(R.string.march),
    APRIL(R.string.april),
    MAY(R.string.may),
    JUNE(R.string.june),
    JULY(R.string.july),
    AUGUST(R.string.august),
    SEPTEMBER(R.string.september),
    OCTOBER(R.string.october),
    NOVEMBER(R.string.november),
    DECEMBER(R.string.december);

    fun getName(ctx: Context) = ctx.resources.getString(monthResId)
}
