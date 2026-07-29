package com.example.ui.util

object LanguageUtils {

    fun formatNumber(number: Any, isBangla: Boolean): String {
        val str = number.toString()
        if (!isBangla) return str

        val enToBnMap = mapOf(
            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
        )
        return str.map { enToBnMap[it] ?: it }.joinToString("")
    }

    fun formatAmount(amount: Double, currency: String, isBangla: Boolean): String {
        val formattedNumber = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format("%.2f", amount)
        }
        val localizedNum = formatNumber(formattedNumber, isBangla)
        return "$currency $localizedNum"
    }

    fun getText(key: String, isBangla: Boolean): String {
        return if (isBangla) banglaMap[key] ?: englishMap[key] ?: key
        else englishMap[key] ?: key
    }

    private val englishMap = mapOf(
        "app_title" to "WiFi Bill Collection Manager",
        "dashboard" to "Dashboard",
        "customers" to "Customers",
        "payments" to "Payments",
        "payment_history" to "History",
        "reports" to "Reports",
        "settings" to "Settings",
        "total_customers" to "Total Customers",
        "active_customers" to "Active Customers",
        "paid_customers" to "Paid (This Month)",
        "due_customers" to "Due Customers",
        "monthly_collection" to "Monthly Collection",
        "total_due_amount" to "Total Outstanding Due",
        "quick_actions" to "Quick Actions",
        "add_customer" to "Add Customer",
        "collect_payment" to "Pay",
        "search_customer" to "Search customer name, phone...",
        "all" to "All",
        "paid" to "Paid",
        "due" to "Due",
        "partial" to "Partial",
        "active" to "Active",
        "inactive" to "Inactive",
        "notes" to "Notes",
        "package_name" to "Package Name",
        "bill_amount" to "Monthly Bill Amount",
        "full_name" to "Full Name",
        "mobile_number" to "Mobile Number",
        "address" to "Address",
        "connection_date" to "Connection Date",
        "save" to "Save",
        "cancel" to "Cancel",
        "delete" to "Delete",
        "edit" to "Edit",
        "payment_method" to "Payment Method",
        "amount_paid" to "Amount Paid",
        "billing_month" to "Billing Month",
        "payment_date" to "Payment Date",
        "remarks" to "Remarks",
        "owner_name" to "Owner Name",
        "business_name" to "WiFi Business Name",
        "currency" to "Currency Symbol",
        "language" to "Language / ভাষা",
        "pin_lock" to "Security PIN Lock",
        "set_pin" to "Set 4-Digit PIN",
        "backup_restore" to "Backup & Restore",
        "backup_json" to "Backup Database (JSON)",
        "restore_json" to "Restore Database (JSON)",
        "export_csv" to "Export Customers (CSV)",
        "enter_pin" to "Enter Security PIN",
        "wrong_pin" to "Incorrect PIN code. Please try again.",
        "todays_collection" to "Today's Collection",
        "this_month_collection" to "This Month Collection",
        "year_collection" to "Yearly Collection",
        "income_summary" to "Income & Billing Summary",
        "receipt" to "Payment Receipt",
        "status" to "Status",
        "last_payment_date" to "Last Payment Date",
        "outstanding_due" to "Outstanding Due"
    )

    private val banglaMap = mapOf(
        "app_title" to "ওয়াইফাই বিল কালেকশন ম্যানেজার",
        "dashboard" to "ড্যাশবোর্ড",
        "customers" to "গ্রাহকবৃন্দ",
        "payments" to "পেমেন্টসমূহ",
        "payment_history" to "হিস্টোরি",
        "reports" to "রিপোর্ট",
        "settings" to "সেটিংস",
        "total_customers" to "মোট গ্রাহক",
        "active_customers" to "সক্রিয় গ্রাহক",
        "paid_customers" to "পরিশোধিত (চলতি মাস)",
        "due_customers" to "বকেয়া গ্রাহক",
        "monthly_collection" to "চলতি মাসের আদায়",
        "total_due_amount" to "মোট সর্বমোট বকেয়া",
        "quick_actions" to "দ্রুত অ্যাকশন",
        "add_customer" to "নতুন গ্রাহক যুক্ত করুন",
        "collect_payment" to "পে করুন",
        "search_customer" to "নাম বা মোবাইল দিয়ে খুঁজুন...",
        "all" to "সকল",
        "paid" to "পরিশোধিত",
        "due" to "বকেয়া",
        "partial" to "আংশিক",
        "active" to "সক্রিয়",
        "inactive" to "নিষ্ক্রিয়",
        "notes" to "নোট",
        "package_name" to "প্যাকেজের নাম",
        "bill_amount" to "মাসিক বিল পরিমাণ",
        "full_name" to "সম্পূর্ণ নাম",
        "mobile_number" to "মোবাইল নম্বর",
        "address" to "ঠিকানা",
        "connection_date" to "সংযোগের তারিখ",
        "save" to "সংরক্ষণ করুন",
        "cancel" to "বাতিল",
        "delete" to "মুছে ফেলুন",
        "edit" to "এডিট",
        "payment_method" to "পেমেন্ট মেথড",
        "amount_paid" to "জমা দেওয়া পরিমাণ",
        "billing_month" to "বিলিং মাস",
        "payment_date" to "পেমেন্টের তারিখ",
        "remarks" to "মন্তব্য",
        "owner_name" to "মালিকের নাম",
        "business_name" to "ওয়াইফাই ব্যবসার নাম",
        "currency" to "মুদ্রার প্রতীক",
        "language" to "ভাষা / Language",
        "pin_lock" to "সিকিউরিটি পিন লক",
        "set_pin" to "৪-ডিজিট পিন সেট করুন",
        "backup_restore" to "ব্যাকআপ এবং রিস্টোর",
        "backup_json" to "ডাটাবেস ব্যাকআপ (JSON)",
        "restore_json" to "ডাটাবেস রিস্টোর (JSON)",
        "export_csv" to "গ্রাহক তালিকা রফতানি (CSV)",
        "enter_pin" to "সিকিউরিটি পিন দিন",
        "wrong_pin" to "ভুল পিন কোড। আবার চেষ্টা করুন।",
        "todays_collection" to "আজকের মোট আদায়",
        "this_month_collection" to "চলতি মাসের আদায়",
        "year_collection" to "বাৎসরিক আদায়",
        "income_summary" to "আয় ও বিলিং সারসংক্ষেপ",
        "receipt" to "পেমেন্ট রশিদ",
        "status" to "স্ট্যাটাস",
        "last_payment_date" to "সর্বশেষ পেমেন্টের তারিখ",
        "outstanding_due" to "মোট বকেয়া"
    )
}
