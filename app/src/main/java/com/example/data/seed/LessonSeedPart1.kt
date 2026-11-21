package com.example.data.seed

import com.example.data.local.entity.LessonEntity
import com.example.domain.model.DialogueLine
import com.example.domain.model.LessonExample
import com.example.domain.model.LessonQuizQuestion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal val seedJson = Json { ignoreUnknownKeys = true }

internal fun lessonEntity(
    id: Int,
    titleEn: String,
    titleBn: String,
    category: String,
    levelTag: String,
    minutes: Int,
    summaryBn: String,
    explanationBn: String,
    examples: List<LessonExample>,
    dialogue: List<DialogueLine>,
    quiz: List<LessonQuizQuestion>
) = LessonEntity(
    id = id,
    orderIndex = id,
    titleEn = titleEn,
    titleBn = titleBn,
    category = category,
    levelTag = levelTag,
    estimatedMinutes = minutes,
    summaryBn = summaryBn,
    explanationBn = explanationBn,
    examplesJson = seedJson.encodeToString(examples),
    dialogueJson = seedJson.encodeToString(dialogue),
    quizJson = seedJson.encodeToString(quiz)
)

object LessonSeedPart1 {
    fun lessons(): List<LessonEntity> = listOf(
        lessonEntity(
            id = 1,
            titleEn = "English Alphabet",
            titleBn = "ইংরেজি বর্ণমালা",
            category = "Foundations",
            levelTag = "Step 1",
            minutes = 6,
            summaryBn = "A থেকে Z পর্যন্ত ২৬টি ইংরেজি বর্ণ, Vowel ও Consonant সহজ বাংলায় শিখুন।",
            explanationBn = "ইংরেজি ভাষায় মোট ২৬টি বর্ণ আছে। এর মধ্যে ৫টি Vowel (A, E, I, O, U) এবং বাকি ২১টি Consonant। Vowel-গুলো বাংলা স্বরবর্ণের মতো শব্দের উচ্চারণ তৈরি করে। প্রতিদিন বড় হাতের (Capital) এবং ছোট হাতের (Small) অক্ষর জোরে জোরে পড়ে অভ্যাস করুন।",
            examples = listOf(
                LessonExample("A for Apple", "A দিয়ে আপেল", "এ ফর অ্যাপল"),
                LessonExample("B for Book", "B দিয়ে বই", "বি ফর বুক"),
                LessonExample("C for Cat", "C দিয়ে বিড়াল", "সি ফর ক্যাট"),
                LessonExample("E for Egg", "E দিয়ে ডিম", "ই ফর এগ"),
                LessonExample("W for Water", "W দিয়ে পানি", "ডব্লিউ ফর ওয়াটার")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Can you say the first three letters?", "তুমি কি প্রথম তিনটি বর্ণ বলতে পারো?"),
                DialogueLine("Student", "Yes! A, B, C.", "হ্যাঁ! এ, বি, সি।"),
                DialogueLine("Teacher", "Very good! Now say a word with B.", "খুব ভালো! এবার B দিয়ে একটি শব্দ বলো।"),
                DialogueLine("Student", "Book.", "বুক (বই)।")
            ),
            quiz = listOf(
                LessonQuizQuestion("ইংরেজিতে মোট কয়টি বর্ণ (Letter) আছে?", listOf("২৪টি", "২৬টি", "২৮টি"), 1, "ইংরেজি বর্ণমালায় A থেকে Z পর্যন্ত ২৬টি বর্ণ আছে।"),
                LessonQuizQuestion("নিচের কোনটি Vowel?", listOf("B", "E", "M"), 1, "A, E, I, O, U হলো ৫টি Vowel।"),
                LessonQuizQuestion("'Book' শব্দটি কোন অক্ষর দিয়ে শুরু হয়?", listOf("B", "D", "P"), 0, "Book শব্দটির প্রথম অক্ষর B।")
            )
        ),
        lessonEntity(
            id = 2,
            titleEn = "Basic Pronunciation",
            titleBn = "মৌলিক উচ্চারণ",
            category = "Foundations",
            levelTag = "Step 2",
            minutes = 7,
            summaryBn = "বাংলাভাষীদের জন্য V/W, F/P, Th এবং S/Sh-এর সঠিক উচ্চারণ কৌশল।",
            explanationBn = "বাংলায় 'ভ' ও 'ও'-এর পার্থক্য ইংরেজির V এবং W-এর মতো নয়। V বলতে উপরের দাঁত নিচের ঠোঁটে হালকা ছুঁয়ে 'ভ' বলুন (যেমন: Very)। আর W বলতে দুই ঠোঁট গোল করে 'উঅ' বলুন (যেমন: Water)। একইভাবে F উচ্চারণে উপরের দাঁত নিচের ঠোঁটে ছুঁয়ে বাতাস ছাড়ুন (Fine)।",
            examples = listOf(
                LessonExample("Very good", "খুব ভালো", "ভেরি গুড (উপরের দাঁত নিচের ঠোঁটে)"),
                LessonExample("Water please", "দয়া করে পানি দিন", "ওয়াটার প্লিজ (ঠোঁট গোল করে)"),
                LessonExample("Thank you", "ধন্যবাদ", "থ্যাংক ইউ (জিহ্বা দাঁতের মাঝে)"),
                LessonExample("I am fine", "আমি ভালো আছি", "আই অ্যাম ফাইন"),
                LessonExample("She is happy", "সে খুশি", "শি ইজ হ্যাপি")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Say 'Very' gently with your teeth on your lip.", "উপরের দাঁত নিচের ঠোঁটে ছুঁয়ে 'Very' বলো।"),
                DialogueLine("Student", "Very.", "ভেরি।"),
                DialogueLine("Teacher", "Excellent! Now round your lips for 'Water'.", "দারুণ! এবার ঠোঁট গোল করে 'Water' বলো।"),
                DialogueLine("Student", "Water.", "ওয়াটার।")
            ),
            quiz = listOf(
                LessonQuizQuestion("'Water' বলার সময় ঠোঁট কেমন থাকবে?", listOf("গোল হবে", "দাঁতে কামড় দেবেন", "দাঁত চেপে রাখবেন"), 0, "W উচ্চারণে দুই ঠোঁট গোল হয়।"),
                LessonQuizQuestion("'Thank you'-তে 'Th' কীভাবে উচ্চারিত হয়?", listOf("জিহ্বার ডগা দাঁতের মাঝে এনে", "ঠোঁট বন্ধ করে", "গলা থেকে"), 0, "Th উচ্চারণে জিহ্বার ডগা উপরের ও নিচের দাঁতের মাঝে আসে।"),
                LessonQuizQuestion("নিচের কোন শব্দটি 'F' দিয়ে শুরু?", listOf("Pine", "Fine", "Vine"), 1, "Fine-এর শুরুতে F আছে।")
            )
        ),
        lessonEntity(
            id = 3,
            titleEn = "Numbers and Counting",
            titleBn = "সংখ্যা ও গণনা",
            category = "Foundations",
            levelTag = "Step 3",
            minutes = 6,
            summaryBn = "১ থেকে ১০০ পর্যন্ত সংখ্যা বলা, বয়স, দাম এবং সময় বলার সহজ নিয়ম।",
            explanationBn = "দৈনন্দিন কথা বলার সময় সংখ্যা খুবই দরকারি—যেমন নিজের বয়স বলা, দোকানে দাম জিজ্ঞেস করা বা ফোন নম্বর বলা। ১ থেকে ২০ ভালোভাবে মুখস্থ করলে বাকিগুলো খুব সহজ হয়ে যায়।",
            examples = listOf(
                LessonExample("One, two, three, four, five", "এক, দুই, তিন, চার, পাঁচ", "ওয়ান, টু, থ্রি, ফোর, ফাইভ"),
                LessonExample("Ten, twenty, fifty, one hundred", "দশ, বিশ, পঞ্চাশ, একশ", "টেন, টুয়েন্টি, ফিফটি, ওয়ান হানড্রেড"),
                LessonExample("I have two brothers.", "আমার দুই ভাই আছে।", "আই হ্যাভ টু ব্রাদার্স"),
                LessonExample("It costs fifty taka.", "এটির দাম পঞ্চাশ টাকা।", "ইট কস্টস ফিফটি টাকা"),
                LessonExample("I am twenty-two years old.", "আমার বয়স বাইশ বছর।", "আই অ্যাম টুয়েন্টি-টু ইয়ার্স ওল্ড")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "How many pens do you have?", "তোমার কাছে কয়টি কলম আছে?"),
                DialogueLine("Student", "I have three pens.", "আমার কাছে তিনটি কলম আছে।"),
                DialogueLine("Teacher", "Great! How much is one pen?", "দারুণ! একটি কলমের দাম কত?"),
                DialogueLine("Student", "Ten taka.", "দশ টাকা।")
            ),
            quiz = listOf(
                LessonQuizQuestion("'পনেরো'-এর ইংরেজি কোনটি?", listOf("Fifty", "Fifteen", "Five"), 1, "১৫ হলো Fifteen এবং ৫০ হলো Fifty।"),
                LessonQuizQuestion("'আমার দুইটি বই আছে'-এর সঠিক ইংরেজি কোনটি?", listOf("I have two books.", "I am two book.", "I has two books."), 0, "সংখ্যা একের বেশি হলে book-এর সাথে s যুক্ত হয়ে books হয়।"),
                LessonQuizQuestion("100-কে ইংরেজিতে কী বলে?", listOf("Ten", "One thousand", "One hundred"), 2, "১০০ = One hundred।")
            )
        ),
        lessonEntity(
            id = 4,
            titleEn = "Greetings",
            titleBn = "অভিবাদন ও শুভেচ্ছা",
            category = "Daily Basics",
            levelTag = "Step 4",
            minutes = 5,
            summaryBn = "সকাল, দুপুর, বিকাল ও বিদায়ের সময় ইংরেজিতে ভদ্রভাবে কথা শুরু করা শিখুন।",
            explanationBn = "কারো সাথে দেখা হলে প্রথমেই হাসিমুখে অভিবাদন জানাতে হয়। সকাল ১২টা পর্যন্ত Good morning, দুপুর ১২টা থেকে বিকাল ৫টা পর্যন্ত Good afternoon এবং বিকাল ৫টার পর Good evening বলুন। বিদায় নেওয়ার সময় Good bye বা Good night বলুন।",
            examples = listOf(
                LessonExample("Good morning!", "শুভ সকাল!", "গুড মর্নিং"),
                LessonExample("Hello! How are you?", "হ্যালো! আপনি কেমন আছেন?", "হ্যালো! হাউ আর ইউ?"),
                LessonExample("I am fine, thank you.", "আমি ভালো আছি, ধন্যবাদ।", "আই অ্যাম ফাইন, থ্যাংক ইউ"),
                LessonExample("Nice to meet you.", "আপনার সাথে দেখা হয়ে ভালো লাগলো।", "নাইস টু মিট ইউ"),
                LessonExample("See you tomorrow.", "আগামীকাল দেখা হবে।", "সি ইউ টুমরো")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Good morning! How are you today?", "শুভ সকাল! আজ কেমন আছো?"),
                DialogueLine("Student", "Good morning! I am fine, thank you. And you?", "শুভ সকাল! আমি ভালো আছি, ধন্যবাদ। আপনি কেমন আছেন?"),
                DialogueLine("Teacher", "I am very well! Have a nice day.", "আমি খুব ভালো আছি! আপনার দিনটি শুভ হোক।")
            ),
            quiz = listOf(
                LessonQuizQuestion("সকাল ৯টায় কারো সাথে দেখা হলে কী বলবেন?", listOf("Good night", "Good morning", "Good evening"), 1, "সকালে দেখা হলে Good morning বলতে হয়।"),
                LessonQuizQuestion("'How are you?'-এর ভদ্র উত্তর কোনটি?", listOf("My name is Rahim.", "I am fine, thank you.", "Good bye."), 1, "কেমন আছেন জিজ্ঞেস করলে 'I am fine, thank you' বলতে হয়।"),
                LessonQuizQuestion("রাতে ঘুমানোর আগে বিদায় নেওয়ার সময় কী বলবেন?", listOf("Good night", "Good afternoon", "Good morning"), 0, "রাতে বিদায় নিতে Good night ব্যবহার করা হয়।")
            )
        ),
        lessonEntity(
            id = 5,
            titleEn = "Introducing Yourself",
            titleBn = "নিজের পরিচয় দেওয়া",
            category = "Daily Basics",
            levelTag = "Step 5",
            minutes = 7,
            summaryBn = "নিজের নাম, ঠিকানা, পেশা এবং পছন্দ সহজ ৩–৪টি বাক্যে ইংরেজিতে বলা শিখুন।",
            explanationBn = "নিজের নাম বলার সময় শুধু 'My name Kishor' না বলে মাঝখানে 'is' যোগ করুন: 'My name is Kishor.' অথবা 'I am Kishor.'। কোথায় থাকেন তা বলতে 'I live in...' এবং কী করেন তা বলতে 'I am a student / teacher / worker.' ব্যবহার করুন।",
            examples = listOf(
                LessonExample("My name is Rahim.", "আমার নাম রহিম।", "মাই নেম ইজ রহিম"),
                LessonExample("I am from Bangladesh.", "আমি বাংলাদেশ থেকে এসেছি।", "আই অ্যাম ফ্রম বাংলাদেশ"),
                LessonExample("I live in Dhaka.", "আমি ঢাকায় থাকি।", "আই লিভ ইন ঢাকা"),
                LessonExample("I am a student.", "আমি একজন ছাত্র।", "আই অ্যাম আ স্টুডেন্ট"),
                LessonExample("I like learning English.", "আমি ইংরেজি শিখতে পছন্দ করি।", "আই লাইক লার্নিং ইংলিশ")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Hello! What is your name?", "হ্যালো! তোমার নাম কী?"),
                DialogueLine("Student", "My name is Nadia.", "আমার নাম নাদিয়া।"),
                DialogueLine("Teacher", "Where do you live, Nadia?", "নাদিয়া, তুমি কোথায় থাকো?"),
                DialogueLine("Student", "I live in Sylhet.", "আমি সিলেটে থাকি।")
            ),
            quiz = listOf(
                LessonQuizQuestion("সঠিক বাক্য কোনটি?", listOf("My name Karim.", "My name is Karim.", "Name is me Karim."), 1, "নাম বলার সময় 'My name is ...' ব্যবহার করতে হয়।"),
                LessonQuizQuestion("'আমি চট্টগ্রামে থাকি'-এর ইংরেজি কী?", listOf("I live in Chattogram.", "I am live Chattogram.", "I living Chattogram."), 0, "'কোথায় থাকি' বলতে 'I live in ...' বলা হয়।"),
                LessonQuizQuestion("'Where are you from?'-এর উত্তর কোনটি?", listOf("I am fine.", "I am from Bangladesh.", "I am twenty."), 1, "Where are you from মানে আপনি কোথা থেকে এসেছেন।")
            )
        ),
        lessonEntity(
            id = 6,
            titleEn = "Family and Relationships",
            titleBn = "পরিবার ও সম্পর্ক",
            category = "Daily Basics",
            levelTag = "Step 6",
            minutes = 6,
            summaryBn = "মা-বাবা, ভাই-বোন এবং পরিবারের সদস্যদের সম্পর্কে ইংরেজিতে কথা বলুন।",
            explanationBn = "পরিবারের সদস্যদের পরিচয় দিতে Father (বাবা), Mother (মা), Brother (ভাই), Sister (বোন), Son (ছেলে), Daughter (মেয়ে) শব্দগুলো ব্যবহার করুন। 'আমার একজন বোন আছে' বলতে 'I have one sister.' বলুন।",
            examples = listOf(
                LessonExample("This is my family.", "এটি আমার পরিবার।", "দিস ইজ মাই ফ্যামিলি"),
                LessonExample("My father is a farmer.", "আমার বাবা একজন কৃষক।", "মাই ফাদার ইজ আ ফার্মার"),
                LessonExample("My mother is very kind.", "আমার মা খুব দয়ালু।", "মাই মাদার ইজ ভেরি কাইন্ড"),
                LessonExample("I have one brother and one sister.", "আমার একজন ভাই ও একজন বোন আছে।", "আই হ্যাভ ওয়ান ব্রাদার অ্যান্ড ওয়ান সিস্টার"),
                LessonExample("I love my family.", "আমি আমার পরিবারকে ভালোবাসি।", "আই লাভ মাই ফ্যামিলি")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "How many people are in your family?", "তোমার পরিবারে কতজন মানুষ আছে?"),
                DialogueLine("Student", "There are four people in my family.", "আমার পরিবারে চারজন মানুষ আছে।"),
                DialogueLine("Teacher", "What does your mother do?", "তোমার মা কী করেন?"),
                DialogueLine("Student", "My mother is a teacher.", "আমার মা একজন শিক্ষিকা।")
            ),
            quiz = listOf(
                LessonQuizQuestion("'বোন'-এর ইংরেজি কী?", listOf("Brother", "Sister", "Aunt"), 1, "Sister অর্থ বোন।"),
                LessonQuizQuestion("'আমার এক ভাই আছে'-এর সঠিক ইংরেজি কোনটি?", listOf("I have one brother.", "I has one brother.", "I am one brother."), 0, "I-এর সাথে have বসে।"),
                LessonQuizQuestion("'My father is a doctor'-এর অর্থ কী?", listOf("আমার ভাই একজন ডাক্তার", "আমার বাবা একজন ডাক্তার", "আমার চাচা একজন ডাক্তার"), 1, "Father অর্থ বাবা।")
            )
        ),
        lessonEntity(
            id = 7,
            titleEn = "Daily Activities",
            titleBn = "দৈনন্দিন কাজ",
            category = "Daily Basics",
            levelTag = "Step 7",
            minutes = 7,
            summaryBn = "সকাল থেকে রাত পর্যন্ত আপনার রোজকার কাজ ইংরেজিতে সহজভাবে বলুন।",
            explanationBn = "প্রতিদিনের কাজ বলার জন্য 'I + verb' কাঠামো ব্যবহার হয়। যেমন: I wake up (আমি ঘুম থেকে উঠি), I eat breakfast (আমি সকালের নাস্তা খাই), I go to work (আমি কাজে যাই)। সময় যোগ করতে 'at 7 AM' ব্যবহার করুন।",
            examples = listOf(
                LessonExample("I wake up at 6 AM.", "আমি সকাল ৬টায় ঘুম থেকে উঠি।", "আই ওয়েক আপ অ্যাট সিক্স এএম"),
                LessonExample("I wash my face and brush my teeth.", "আমি মুখ ধুই এবং দাঁত ব্রাশ করি।", "আই ওয়াশ মাই ফেস অ্যান্ড ব্রাশ মাই টিথ"),
                LessonExample("I eat breakfast at 8 AM.", "আমি সকাল ৮টায় নাস্তা করি।", "আই ইট ব্রেকফাস্ট অ্যাট এইট এএম"),
                LessonExample("I go to work by bus.", "আমি বাসে করে কাজে যাই।", "আই গো টু ওয়ার্ক বাই বাস"),
                LessonExample("I sleep at 10 PM.", "আমি রাত ১০টায় ঘুমাই।", "আই স্লিপ অ্যাট টেন পিএম")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "What time do you wake up?", "তুমি কয়টায় ঘুম থেকে ওঠো?"),
                DialogueLine("Student", "I wake up at seven o'clock.", "আমি সাতটায় ঘুম থেকে উঠি।"),
                DialogueLine("Teacher", "What do you do after breakfast?", "নাস্তার পর তুমি কী করো?"),
                DialogueLine("Student", "I go to school.", "আমি স্কুলে যাই।")
            ),
            quiz = listOf(
                LessonQuizQuestion("'সকালের নাস্তা'-এর ইংরেজি কী?", listOf("Dinner", "Lunch", "Breakfast"), 2, "সকালের নাস্তাকে Breakfast বলে।"),
                LessonQuizQuestion("সময়ের আগে কোন ছোট শব্দটি বসে (যেমন __ 7 AM)?", listOf("at", "on", "to"), 0, "নির্দিষ্ট সময়ের আগে 'at' বসে।"),
                LessonQuizQuestion("'I sleep at night'-এর অর্থ কী?", listOf("আমি রাতে পড়ি", "আমি রাতে ঘুমাই", "আমি সকালে হাঁটি"), 1, "Sleep অর্থ ঘুমানো।")
            )
        ),
        lessonEntity(
            id = 8,
            titleEn = "Common English Words",
            titleBn = "সাধারণ ইংরেজি শব্দ",
            category = "Vocabulary",
            levelTag = "Step 8",
            minutes = 6,
            summaryBn = "প্রতিদিন সবচেয়ে বেশি ব্যবহৃত ৫০টি কাজের ও পরিচিত ইংরেজি শব্দ শিখুন।",
            explanationBn = "ইংরেজিতে কথা শুরু করতে হাজার হাজার শব্দ জানা লাগে না। মাত্র ১০০-২০০টি সাধারণ শব্দ জানলেই দৈনন্দিন কথা বলা যায়। বিশেষ করে Please, Sorry, Thank you, Help, Water, Food, Time, Today, Tomorrow শব্দগুলো প্রতিদিন ব্যবহার করুন।",
            examples = listOf(
                LessonExample("Please help me.", "দয়া করে আমাকে সাহায্য করুন।", "প্লিজ হেল্প মি"),
                LessonExample("Excuse me, where is the station?", "মাফ করবেন, স্টেশনটি কোথায়?", "এক্সকিউজ মি, হোয়্যার ইজ দ্য স্টেশন?"),
                LessonExample("I am sorry.", "আমি দুঃখিত।", "আই অ্যাম সরি"),
                LessonExample("Wait a minute, please.", "দয়া করে এক মিনিট অপেক্ষা করুন।", "ওয়েট আ মিনিট, প্লিজ"),
                LessonExample("I understand now.", "আমি এখন বুঝতে পারছি।", "আই আন্ডারস্ট্যান্ড নাউ")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "If you do not understand, what can you say?", "যদি তুমি বুঝতে না পারো, কী বলবে?"),
                DialogueLine("Student", "Sorry, please say that again.", "দুঃখিত, দয়া করে আবার বলুন।"),
                DialogueLine("Teacher", "Polite and clear! Well done.", "ভদ্র ও স্পষ্ট! সাবাস।")
            ),
            quiz = listOf(
                LessonQuizQuestion("কারো দৃষ্টি আকর্ষণ করতে ভদ্রভাবে কী বলবেন?", listOf("Excuse me", "Go away", "Never"), 0, "ভদ্রভাবে মনোযোগ আকর্ষণ করতে Excuse me বলে।"),
                LessonQuizQuestion("'দয়া করে'-এর ইংরেজি কী?", listOf("Sorry", "Please", "Thanks"), 1, "Please অর্থ দয়া করে।"),
                LessonQuizQuestion("'Tomorrow' শব্দের অর্থ কী?", listOf("গতকাল", "আজ", "আগামীকাল"), 2, "Today = আজ, Yesterday = গতকাল, Tomorrow = আগামীকাল।")
            )
        ),
        lessonEntity(
            id = 9,
            titleEn = "Basic Sentence Formation",
            titleBn = "সহজ বাক্য গঠন",
            category = "Grammar",
            levelTag = "Step 9",
            minutes = 8,
            summaryBn = "Subject + Verb + Object নিয়ম ব্যবহার করে নিজেই সঠিক ইংরেজি বাক্য বানান।",
            explanationBn = "বাংলায় আমরা বলি 'আমি ভাত খাই' (কর্তা + কর্ম + ক্রিয়া)। কিন্তু ইংরেজিতে ক্রিয়া (Verb) মাঝখানে আসে: 'I eat rice' (কর্তা + ক্রিয়া + কর্ম)। এই ছোট সূত্রটি মনে রাখলে হাজারো বাক্য বানাতে পারবেন!",
            examples = listOf(
                LessonExample("I drink water.", "আমি পানি পান করি।", "আই ড্রিংক ওয়াটার"),
                LessonExample("She reads a book.", "সে একটি বই পড়ে।", "শি রিডস আ বুক"),
                LessonExample("We speak English.", "আমরা ইংরেজি বলি।", "উই স্পিক ইংলিশ"),
                LessonExample("He likes tea.", "সে চা পছন্দ করে।", "হি লাইকস টি"),
                LessonExample("They play football.", "তারা ফুটবল খেলে।", "দে প্লে ফুটবল")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Make a sentence with 'I' and 'eat'.", "'I' এবং 'eat' দিয়ে একটি বাক্য তৈরি করো।"),
                DialogueLine("Student", "I eat rice.", "আমি ভাত খাই।"),
                DialogueLine("Teacher", "Perfect! Now make one with 'She'.", "একদম ঠিক! এবার 'She' দিয়ে একটি বানাও।"),
                DialogueLine("Student", "She eats mango.", "সে আম খায়।")
            ),
            quiz = listOf(
                LessonQuizQuestion("ইংরেজি বাক্যের সঠিক ক্রম কোনটি?", listOf("I rice eat", "I eat rice", "Eat I rice"), 1, "ইংরেজিতে Subject + Verb + Object ক্রম অনুসরণ করা হয়।"),
                LessonQuizQuestion("'He' বা 'She'-এর পরের verb-এ কী হয় (বর্তমান কালে)?", listOf("s/es যোগ হয়", "কিছু হয় না", "ing যোগ হয়"), 0, "He/She/It এর পর verb-এ সাধারণত s বা es যোগ হয় (যেমন: She reads)।"),
                LessonQuizQuestion("'আমরা ইংরেজি শিখি'-এর সঠিক ইংরেজি কোনটি?", listOf("We learn English.", "We English learn.", "Us learn English."), 0, "We + learn + English।")
            )
        ),
        lessonEntity(
            id = 10,
            titleEn = "Present Tense",
            titleBn = "বর্তমান কাল",
            category = "Grammar",
            levelTag = "Step 10",
            minutes = 8,
            summaryBn = "এখন যা হচ্ছে (I am speaking) এবং যা নিয়মিত হয় (I speak) — এই দুটির সহজ পার্থক্য।",
            explanationBn = "বর্তমান সময়ের কথা বলতে আমরা দুইভাবে বলি: ১) নিয়মিত অভ্যাস: 'I work in an office.' (আমি অফিসে কাজ করি)। ২) ঠিক এই মুহূর্তে যা ঘটছে: 'I am working now.' (আমি এখন কাজ করছি)। 'এখন করছি' বোঝাতে am/is/are-এর সাথে verb + ing যোগ করুন।",
            examples = listOf(
                LessonExample("I speak English every day.", "আমি প্রতিদিন ইংরেজি বলি।", "আই স্পিক ইংলিশ এভরি ডে"),
                LessonExample("I am speaking English now.", "আমি এখন ইংরেজি বলছি।", "আই অ্যাম স্পিকিং ইংলিশ নাউ"),
                LessonExample("He works in a bank.", "তিনি একটি ব্যাংকে কাজ করেন।", "হি ওয়ার্কস ইন আ ব্যাংক"),
                LessonExample("She is cooking dinner.", "তিনি রাতের খাবার রান্না করছেন।", "শি ইজ কুকিং ডিনার"),
                LessonExample("Are you learning English?", "আপনি কি ইংরেজি শিখছেন?", "আর ইউ লার্নিং ইংলিশ?")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "What are you doing right now?", "তুমি ঠিক এখন কী করছো?"),
                DialogueLine("Student", "I am practicing English with you.", "আমি আপনার সাথে ইংরেজি চর্চা করছি।"),
                DialogueLine("Teacher", "Wonderful! Do you practice every day?", "দারুণ! তুমি কি প্রতিদিন চর্চা করো?"),
                DialogueLine("Student", "Yes, I practice every morning.", "হ্যাঁ, আমি প্রতিদিন সকালে চর্চা করি।")
            ),
            quiz = listOf(
                LessonQuizQuestion("'আমি এখন পড়ছি'-এর সঠিক ইংরেজি কোনটি?", listOf("I read now.", "I am reading now.", "I reading now."), 1, "এই মুহূর্তে চলছে বোঝাতে am/is/are + verb-ing বসে।"),
                LessonQuizQuestion("'She ___ to school every day.' শূন্যস্থানে কী বসবে?", listOf("go", "goes", "going"), 1, "She-এর সাথে goes বসে।"),
                LessonQuizQuestion("'They are playing'-এর বাংলা কী?", listOf("তারা খেলেছিল", "তারা খেলছে", "তারা খেলবে"), 1, "Are playing = এখন খেলছে।")
            )
        )
    )
}
