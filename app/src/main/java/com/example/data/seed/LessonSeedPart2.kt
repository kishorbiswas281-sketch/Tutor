package com.example.data.seed

import com.example.data.local.entity.LessonEntity
import com.example.domain.model.DialogueLine
import com.example.domain.model.LessonExample
import com.example.domain.model.LessonQuizQuestion

object LessonSeedPart2 {
    fun lessons(): List<LessonEntity> = listOf(
        lessonEntity(
            id = 11,
            titleEn = "Past Tense",
            titleBn = "অতীত কাল",
            category = "Grammar",
            levelTag = "Step 11",
            minutes = 8,
            summaryBn = "গতকাল বা আগে কী ঘটেছিল তা সহজ Past Tense-এ বলার নিয়ম শিখুন।",
            explanationBn = "অতীতের কোনো ঘটনা বলতে Verb-এর Past রূপ ব্যবহার করতে হয়। অনেক Verb-এর শেষে '-ed' যোগ করলেই Past হয়ে যায় (যেমন: walk -> walked, play -> played)। কিছু সাধারণ Verb বদলে যায়: go -> went, eat -> ate, see -> saw। না বোঝাতে 'did not' + মূল Verb বসান।",
            examples = listOf(
                LessonExample("I went to the market yesterday.", "আমি গতকাল বাজারে গিয়েছিলাম।", "আই ওয়েন্ট টু দ্য মার্কেট ইয়েস্টারডে"),
                LessonExample("I ate rice for lunch.", "আমি দুপুরে ভাত খেয়েছিলাম।", "আই এইট রাইস ফর লাঞ্চ"),
                LessonExample("She called me last night.", "সে গত রাতে আমাকে ফোন করেছিল।", "শি কলড মি লাস্ট নাইট"),
                LessonExample("We watched a cricket match.", "আমরা একটি ক্রিকেট ম্যাচ দেখেছিলাম।", "উই ওয়াচড আ ক্রিকেট ম্যাচ"),
                LessonExample("I did not go to office.", "আমি অফিসে যাইনি।", "আই ডিড নট গো টু অফিস")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "What did you do yesterday?", "গতকাল তুমি কী করেছিলে?"),
                DialogueLine("Student", "I visited my friend.", "আমি আমার বন্ধুর সাথে দেখা করেছিলাম।"),
                DialogueLine("Teacher", "Did you have a good time?", "তোমার সময়টা কি ভালো কেটেছিল?"),
                DialogueLine("Student", "Yes, we talked and drank tea.", "হ্যাঁ, আমরা গল্প করেছি এবং চা পান করেছি।")
            ),
            quiz = listOf(
                LessonQuizQuestion("'Go' শব্দের Past রূপ কী?", listOf("Goed", "Went", "Gone"), 1, "Go-এর Past রূপ হলো Went।"),
                LessonQuizQuestion("'আমি গতকাল যাইনি'-এর সঠিক ইংরেজি কোনটি?", listOf("I did not went.", "I did not go.", "I no go."), 1, "'did not'-এর পর সবসময় মূল Verb (go) বসে।"),
                LessonQuizQuestion("'Eat'-এর Past রূপ কোনটি?", listOf("Ate", "Eated", "Eating"), 0, "Eat -> Ate।")
            )
        ),
        lessonEntity(
            id = 12,
            titleEn = "Future Tense",
            titleBn = "ভবিষ্যৎ কাল",
            category = "Grammar",
            levelTag = "Step 12",
            minutes = 7,
            summaryBn = "আগামীকাল বা সামনে কী করবেন তা 'will' এবং 'going to' দিয়ে সহজে বলুন।",
            explanationBn = "ভবিষ্যতে কোনো কাজ করবেন বোঝাতে Verb-এর আগে শুধু 'will' বসিয়ে দিন! যেমন: I will go (আমি যাব), I will call you (আমি তোমাকে ফোন করব)। আর পূর্বপরিকল্পিত কাজের জন্য 'I am going to...' ব্যবহার করতে পারেন।",
            examples = listOf(
                LessonExample("I will learn English.", "আমি ইংরেজি শিখব।", "আই উইল লার্ন ইংলিশ"),
                LessonExample("I will call you tomorrow.", "আমি তোমাকে আগামীকাল ফোন করব।", "আই উইল কল ইউ টুমরো"),
                LessonExample("We will go to Cox's Bazar.", "আমরা কক্সবাজার যাব।", "উই উইল গো টু কক্সবাজার"),
                LessonExample("It will rain today.", "আজ বৃষ্টি হবে।", "ইট উইল রেইন টুডে"),
                LessonExample("I will not forget.", "আমি ভুলব না।", "আই উইল নট ফরগেট")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "What will you do this weekend?", "এই ছুটির দিনে তুমি কী করবে?"),
                DialogueLine("Student", "I will visit my grandmother.", "আমি আমার দাদির বাড়িতে যাব।"),
                DialogueLine("Teacher", "That sounds lovely! Will you practice English too?", "খুব সুন্দর! তুমি কি ইংরেজিও চর্চা করবে?"),
                DialogueLine("Student", "Yes, I will practice every day.", "হ্যাঁ, আমি প্রতিদিন চর্চা করব।")
            ),
            quiz = listOf(
                LessonQuizQuestion("ভবিষ্যৎ বোঝাতে Verb-এর আগে সাধারণত কী বসে?", listOf("did", "will", "was"), 1, "ভবিষ্যৎ কালে 'will' বসে।"),
                LessonQuizQuestion("'সে আগামীকাল আসবে'-এর সঠিক ইংরেজি কী?", listOf("He will come tomorrow.", "He comes yesterday.", "He will came tomorrow."), 0, "Will-এর পর মূল verb (come) বসে।"),
                LessonQuizQuestion("'I will help you'-এর অর্থ কী?", listOf("আমি তোমাকে সাহায্য করেছিলাম", "আমি তোমাকে সাহায্য করব", "তুমি আমাকে সাহায্য করো"), 1, "Will help = সাহায্য করব।")
            )
        ),
        lessonEntity(
            id = 13,
            titleEn = "Questions and Answers",
            titleBn = "প্রশ্ন ও উত্তর",
            category = "Conversation",
            levelTag = "Step 13",
            minutes = 8,
            summaryBn = "What, Where, When, Who, Why, How দিয়ে সহজে প্রশ্ন করা ও উত্তর দেওয়া শিখুন।",
            explanationBn = "ইংরেজিতে প্রশ্ন করতে ৫টি W এবং ১টি H খুবই কাজের: What (কী), Where (কোথায়), When (কখন), Who (কে), Why (কেন), How (কীভাবে/কত)। মনে রাখবেন, প্রশ্নে সাহায্যকারী Verb (is, are, do, did) কর্তার আগে চলে আসে।",
            examples = listOf(
                LessonExample("What is this?", "এটি কী?", "হোয়াট ইজ দিস?"),
                LessonExample("Where do you work?", "আপনি কোথায় কাজ করেন?", "হোয়্যার ডু ইউ ওয়ার্ক?"),
                LessonExample("When does the bus leave?", "বাসটি কখন ছাড়ে?", "হোয়েন ডাজ দ্য বাস লিভ?"),
                LessonExample("Who is your teacher?", "আপনার শিক্ষক কে?", "হু ইজ ইয়োর টিচার?"),
                LessonExample("How much does this cost?", "এটির দাম কত?", "হাউ মাচ ডাজ দিস কস্ট?")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Ask me where I live.", "আমি কোথায় থাকি তা আমাকে জিজ্ঞেস করো।"),
                DialogueLine("Student", "Where do you live?", "আপনি কোথায় থাকেন?"),
                DialogueLine("Teacher", "Great question! I live in Dhaka. Now ask why I teach.", "দারুণ প্রশ্ন! আমি ঢাকায় থাকি। এবার জিজ্ঞেস করো কেন আমি পড়াই।"),
                DialogueLine("Student", "Why do you teach English?", "আপনি কেন ইংরেজি শেখান?")
            ),
            quiz = listOf(
                LessonQuizQuestion("'কোথায়' বোঝাতে কোন প্রশ্ন-শব্দ ব্যবহার হয়?", listOf("When", "Where", "Who"), 1, "Where অর্থ কোথায়।"),
                LessonQuizQuestion("'কখন' বোঝাতে কোনটি ব্যবহার হয়?", listOf("When", "Why", "What"), 0, "When অর্থ কখন।"),
                LessonQuizQuestion("দাম জিজ্ঞেস করতে কোনটি বলবেন?", listOf("How many?", "How much?", "Who much?"), 1, "দাম জিজ্ঞেস করতে 'How much?' ব্যবহার হয়।")
            )
        ),
        lessonEntity(
            id = 14,
            titleEn = "Shopping Conversations",
            titleBn = "দোকানে কেনাকাটা",
            category = "Real Life",
            levelTag = "Step 14",
            minutes = 7,
            summaryBn = "দোকানে জিনিস খোঁজা, দাম জিজ্ঞেস করা, সাইজ চাওয়া ও পেমেন্ট করার ইংরেজি শিখুন।",
            explanationBn = "দোকানে গিয়ে বিনয়ীভাবে কোনো কিছু চাইতে 'I am looking for...' (আমি খুঁজছি) অথবা 'Do you have...?' (আপনার কাছে কি আছে?) ব্যবহার করুন। দাম জানতে 'How much is this?' বলুন।",
            examples = listOf(
                LessonExample("Do you have fresh apples?", "আপনার কাছে কি তাজা আপেল আছে?", "ডু ইউ হ্যাভ ফ্রেশ অ্যাপলস?"),
                LessonExample("How much is this shirt?", "এই শার্টটির দাম কত?", "হাউ মাচ ইজ দিস শার্ট?"),
                LessonExample("Can I get a smaller size?", "আমি কি একটু ছোট সাইজ পেতে পারি?", "ক্যান আই গেট আ স্মলার সাইজ?"),
                LessonExample("That is a bit expensive.", "এটার দাম একটু বেশি মনে হচ্ছে।", "দ্যাট ইজ আ বিট এক্সপেনসিভ"),
                LessonExample("I will take this one, please.", "দয়া করে আমি এটি নেব।", "আই উইল টেক দিস ওয়ান, প্লিজ")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Welcome to my shop! How can I help you?", "আমার দোকানে স্বাগতম! আপনাকে কীভাবে সাহায্য করতে পারি?"),
                DialogueLine("Student", "I am looking for a blue shirt.", "আমি একটি নীল শার্ট খুঁজছি।"),
                DialogueLine("Teacher", "Here is one in medium size. It is 500 taka.", "এই যে মিডিয়াম সাইজের একটি। এর দাম ৫০০ টাকা।"),
                DialogueLine("Student", "Thank you. I will take it.", "ধন্যবাদ। আমি এটি নেব।")
            ),
            quiz = listOf(
                LessonQuizQuestion("দোকানদারকে কোন কিছুর দাম জিজ্ঞেস করতে কী বলবেন?", listOf("How much is this?", "Where is this?", "Who is this?"), 0, "How much is this? মানে এটির দাম কত?"),
                LessonQuizQuestion("'আমি এটি নেব'-এর ইংরেজি কী?", listOf("I will take this.", "I will give this.", "I take yesterday."), 0, "I will take this = আমি এটি নেব।"),
                LessonQuizQuestion("'Expensive' শব্দের অর্থ কী?", listOf("সস্তা", "দামি", "নতুন"), 1, "Expensive অর্থ দামি।")
            )
        ),
        lessonEntity(
            id = 15,
            titleEn = "Restaurant Conversations",
            titleBn = "রেস্তোরাঁয় কথোপকথন",
            category = "Real Life",
            levelTag = "Step 15",
            minutes = 7,
            summaryBn = "মেনু চাওয়া, খাবার অর্ডার দেওয়া, পানি চাওয়া এবং বিল পরিশোধ করার সহজ বাক্য।",
            explanationBn = "রেস্তোরাঁয় খাবার অর্ডার দিতে 'I would like...' (আমি নিতে চাই) ব্যবহার করা সবচেয়ে ভদ্র পদ্ধতি। যেমন: 'I would like chicken biryani and a bottle of water.'। শেষে বিল চাইতে বলুন: 'The bill, please.'",
            examples = listOf(
                LessonExample("Can I see the menu, please?", "দয়া করে মেনুটি দেখতে পারি কি?", "ক্যান আই সি দ্য মেনু, প্লিজ?"),
                LessonExample("I would like rice and fish curry.", "আমি ভাত এবং মাছের তরকারি নিতে চাই।", "আই উড লাইক রাইস অ্যান্ড ফিশ কারি"),
                LessonExample("Could I have a glass of water?", "আমি কি এক গ্লাস পানি পেতে পারি?", "কুড আই হ্যাভ আ গ্লাস অব ওয়াটার?"),
                LessonExample("The food is very delicious!", "খাবারটি খুব সুস্বাদু!", "দ্য ফুড ইজ ভেরি ডেলিশিয়াস"),
                LessonExample("Excuse me, can I have the bill?", "মাফ করবেন, বিলটা দেবেন কি?", "এক্সকিউজ মি, ক্যান আই হ্যাভ দ্য বিল?")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Hello! Are you ready to order?", "হ্যালো! আপনি কি অর্ডার দিতে প্রস্তুত?"),
                DialogueLine("Student", "Yes. I would like fried rice and tea, please.", "হ্যাঁ। দয়া করে আমাকে ফ্রাইড রাইস এবং চা দিন।"),
                DialogueLine("Teacher", "Certainly! Anything else?", "অবশ্যই! আর কিছু লাগবে?"),
                DialogueLine("Student", "No, thank you. Just water.", "না, ধন্যবাদ। শুধু পানি।")
            ),
            quiz = listOf(
                LessonQuizQuestion("খাবার অর্ডার দেওয়ার ভদ্র বাক্য কোনটি?", listOf("Give me rice now.", "I would like rice, please.", "Rice want me."), 1, "ভদ্রভাবে অর্ডার দিতে 'I would like ... please' ব্যবহার হয়।"),
                LessonQuizQuestion("'Delicious' অর্থ কী?", listOf("ঝাল", "সুস্বাদু", "ঠান্ডা"), 1, "Delicious অর্থ সুস্বাদু।"),
                LessonQuizQuestion("খাওয়া শেষে টাকা দেওয়ার জন্য কী চাইবেন?", listOf("The menu, please.", "The bill, please.", "The spoon, please."), 1, "খাবারের হিসাবকে bill বা check বলে।")
            )
        ),
        lessonEntity(
            id = 16,
            titleEn = "Office Conversations",
            titleBn = "অফিসে কথোপকথন",
            category = "Workplace",
            levelTag = "Step 16",
            minutes = 8,
            summaryBn = "সহকর্মী ও বসের সাথে সৌজন্যমূলক কথা, মিটিং, সাহায্য চাওয়া এবং কাজের আপডেট দেওয়া।",
            explanationBn = "অফিসে পরিষ্কার ও পেশাদার ইংরেজি ব্যবহার জরুরি। সাহায্য চাইতে 'Could you please help me with this?' এবং কাজ শেষ হলে 'I have finished the report.' বলুন। কোনো ভুল হলে সহজভাবে বলুন 'Sorry for the delay.'",
            examples = listOf(
                LessonExample("Good morning, everyone.", "সবাইকে শুভ সকাল।", "গুড মর্নিং, এভরিওয়ান"),
                LessonExample("Could you please check this file?", "আপনি কি দয়া করে এই ফাইলটি দেখবেন?", "কুড ইউ প্লিজ চেক দিস ফাইল?"),
                LessonExample("I am working on it right now.", "আমি এখনই এটির কাজ করছি।", "আই অ্যাম ওয়ার্কিং অন ইট রাইট নাউ"),
                LessonExample("I have sent the email.", "আমি ইমেইলটি পাঠিয়ে দিয়েছি।", "আই হ্যাভ সেন্ট দ্য ইমেইল"),
                LessonExample("Let us have a short meeting.", "চলুন একটি সংক্ষিপ্ত মিটিং করি।", "লেট আস হ্যাভ আ শর্ট মিটিং")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Did you finish the daily report?", "তুমি কি দৈনিক রিপোর্টটি শেষ করেছো?"),
                DialogueLine("Student", "Yes, I sent it by email.", "হ্যাঁ, আমি ইমেইলে পাঠিয়ে দিয়েছি।"),
                DialogueLine("Teacher", "Thank you! Please join the meeting at 3 PM.", "ধন্যবাদ! দয়া করে বিকাল ৩টার মিটিংয়ে যোগ দিও।")
            ),
            quiz = listOf(
                LessonQuizQuestion("সহকর্মীর কাছে ভদ্রভাবে সাহায্য চাইতে কী বলবেন?", listOf("Help me fast.", "Could you please help me?", "You must help."), 1, "Could you please... হলো ভদ্র অনুরোধ।"),
                LessonQuizQuestion("'দেরির জন্য দুঃখিত'-এর ইংরেজি কোনটি?", listOf("Sorry for the delay.", "Happy for late.", "Delay is good."), 0, "Sorry for the delay অর্থ দেরির জন্য দুঃখিত।"),
                LessonQuizQuestion("'I have finished'-এর বাংলা কী?", listOf("আমি শুরু করেছি", "আমি শেষ করেছি", "আমি চেষ্টা করছি"), 1, "Finish অর্থ শেষ করা।")
            )
        ),
        lessonEntity(
            id = 17,
            titleEn = "Travel Conversations",
            titleBn = "ভ্রমণে কথোপকথন",
            category = "Real Life",
            levelTag = "Step 17",
            minutes = 7,
            summaryBn = "বাস/ট্রেন/বিমানবন্দরে টিকিট কাটা, দিকনির্দেশনা চাওয়া এবং হোটেল চেক-ইন শেখা।",
            explanationBn = "ভ্রমণের সময় রাস্তা বা স্টেশন চিনতে 'Excuse me, how do I get to...?' ব্যবহার করুন। টিকিট কিনতে 'One ticket to...' এবং হোটেলে 'I have a reservation.' বলুন।",
            examples = listOf(
                LessonExample("Where is the bus stop?", "বাস স্টপটি কোথায়?", "হোয়্যার ইজ দ্য বাস স্টপ?"),
                LessonExample("One ticket to Dhaka, please.", "দয়া করে ঢাকার একটি টিকিট দিন।", "ওয়ান টিকেট টু ঢাকা, প্লিজ"),
                LessonExample("What time does the train arrive?", "ট্রেনটি কয়টায় পৌঁছাবে?", "হোয়াট টাইম ডাজ দ্য ট্রেন অ্যারাইভ?"),
                LessonExample("Please stop here.", "দয়া করে এখানে থামুন।", "প্লিজ স্টপ হিয়ার"),
                LessonExample("I need a room for two nights.", "আমার দুই রাতের জন্য একটি রুম দরকার।", "আই নিড আ রুম ফর টু নাইটস")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Excuse me, where are you travelling today?", "মাফ করবেন, আপনি আজ কোথায় যাচ্ছেন?"),
                DialogueLine("Student", "I am going to Chattogram by train.", "আমি ট্রেনে করে চট্টগ্রাম যাচ্ছি।"),
                DialogueLine("Teacher", "Platform two is on your left. Have a safe trip!", "দুই নম্বর প্ল্যাটফর্ম আপনার বাম দিকে। নিরাপদ যাত্রা হোক!")
            ),
            quiz = listOf(
                LessonQuizQuestion("'প্ল্যাটফর্মটি কোথায়?'-এর ইংরেজি কী?", listOf("Where is the platform?", "When is the platform?", "Who is the platform?"), 0, "জায়গা জানতে Where ব্যবহার হয়।"),
                LessonQuizQuestion("'Arrive' শব্দের অর্থ কী?", listOf("ছেড়ে যাওয়া", "পৌঁছানো", "বাতিল হওয়া"), 1, "Arrive মানে পৌঁছানো, Leave মানে ছেড়ে যাওয়া।"),
                LessonQuizQuestion("গাড়ি বা রিকশা থামাতে কী বলবেন?", listOf("Go fast", "Please stop here", "Turn left"), 1, "Please stop here = দয়া করে এখানে থামুন।")
            )
        ),
        lessonEntity(
            id = 18,
            titleEn = "Telephone Conversations",
            titleBn = "ফোনে কথোপকথন",
            category = "Workplace",
            levelTag = "Step 18",
            minutes = 7,
            summaryBn = "ফোন ধরা, নিজের পরিচয় দেওয়া, কথা স্পষ্ট না শোনা গেলে আবার বলতে বলা শিখুন।",
            explanationBn = "ফোনে কথা বলার সময় আমরা মুখ দেখতে পাই না, তাই ভদ্রভাবে নিজের নাম বলা জরুরি: 'Hello, this is...'। নেটওয়ার্ক দুর্বল হলে বলুন 'Sorry, I cannot hear you clearly. Could you speak a little slower?'",
            examples = listOf(
                LessonExample("Hello, this is Tanvir speaking.", "হ্যালো, আমি তানভীর বলছি।", "হ্যালো, দিস ইজ তানভীর স্পিকিং"),
                LessonExample("May I speak to Mr. Hasan?", "আমি কি জনাব হাসানের সাথে কথা বলতে পারি?", "মে আই স্পিক টু মিস্টার হাসান?"),
                LessonExample("Please hold on for a moment.", "দয়া করে একটু লাইনে থাকুন।", "প্লিজ হোল্ড অন ফর আ মোমেন্ট"),
                LessonExample("Sorry, your voice is breaking up.", "দুঃখিত, আপনার কথা কেটে কেটে আসছে।", "সরি, ইয়োর ভয়েস ইজ ব্রেকিং আপ"),
                LessonExample("I will call you back soon.", "আমি কিছুক্ষণের মধ্যেই আবার ফোন করব।", "আই উইল কল ইউ ব্যাক সুন")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Ring ring! Hello, who is speaking?", "হ্যালো, কে বলছেন?"),
                DialogueLine("Student", "Hello, this is Rashed speaking.", "হ্যালো, আমি রাশেদ বলছি।"),
                DialogueLine("Teacher", "Hi Rashed! Sorry, I could not hear the last word.", "হাই রাশেদ! দুঃখিত, শেষ কথাটা শুনতে পাইনি।"),
                DialogueLine("Student", "No problem. I will repeat it.", "কোনো সমস্যা নেই। আমি আবার বলছি।")
            ),
            quiz = listOf(
                LessonQuizQuestion("ফোনে নিজের পরিচয় দিতে কোনটি সবচেয়ে স্বাভাবিক?", listOf("This is Arif speaking.", "He is Arif.", "Me Arif phone."), 0, "ফোনে 'This is [নাম] speaking' ব্যবহার করা হয়।"),
                LessonQuizQuestion("'একটু অপেক্ষা করুন' ফোনে কীভাবে বলবেন?", listOf("Please hold on.", "Please hang up.", "Please run."), 0, "Hold on মানে লাইনে অপেক্ষা করা।"),
                LessonQuizQuestion("কথা স্পষ্ট না হলে কী বলবেন?", listOf("Could you speak slower, please?", "Never call me.", "Speak very fast."), 0, "আস্তে ও স্পষ্ট বলতে অনুরোধ করতে 'Could you speak slower, please?' বলুন।")
            )
        ),
        lessonEntity(
            id = 19,
            titleEn = "Interview English",
            titleBn = "ইন্টারভিউ ইংরেজি",
            category = "Workplace",
            levelTag = "Step 19",
            minutes = 9,
            summaryBn = "চাকরির ইন্টারভিউয়ে নিজের পরিচয়, দক্ষতা এবং আগ্রহ আত্মবিশ্বাসের সাথে বলুন।",
            explanationBn = "ইন্টারভিউয়ে সবচেয়ে সাধারণ প্রশ্ন হলো 'Tell me about yourself.'। এর উত্তরে ৩টি সহজ ধাপ বলুন: ১) নাম ও পড়াশোনা/কাজ, ২) আপনার ২টি ভালো গুণ (যেমন: hardworking, honest), ৩) কেন কাজটি করতে চান।",
            examples = listOf(
                LessonExample("Thank you for this opportunity.", "এই সুযোগের জন্য ধন্যবাদ।", "থ্যাংক ইউ ফর দিস অপরচুনিটি"),
                LessonExample("I am a hardworking and honest person.", "আমি একজন পরিশ্রমী এবং সৎ মানুষ।", "আই অ্যাম আ হার্ডওয়ার্কিং অ্যান্ড অনেস্ট পারসন"),
                LessonExample("I have two years of experience.", "আমার দুই বছরের কাজের অভিজ্ঞতা আছে।", "আই হ্যাভ টু ইয়ার্স অব এক্সপেরিয়েন্স"),
                LessonExample("I learn new skills quickly.", "আমি নতুন কাজ দ্রুত শিখতে পারি।", "আই লার্ন নিউ স্কিলস কুইকলি"),
                LessonExample("I want to grow with your team.", "আমি আপনাদের দলের সাথে কাজ করে উন্নতি করতে চাই।", "আই ওয়ান্ট টু গ্রো উইথ ইয়োর টিম")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "Welcome! Please tell me about yourself.", "স্বাগতম! অনুগ্রহ করে নিজের সম্পর্কে বলুন।"),
                DialogueLine("Student", "Thank you. My name is Sumon. I am hardworking and eager to learn.", "ধন্যবাদ। আমার নাম সুমন। আমি পরিশ্রমী এবং শিখতে আগ্রহী।"),
                DialogueLine("Teacher", "Why do you want this job?", "আপনি এই চাকরিটি কেন চান?"),
                DialogueLine("Student", "I like this work and I want to do my best.", "আমি এই কাজ পছন্দ করি এবং আমার সেরাটা দিতে চাই।")
            ),
            quiz = listOf(
                LessonQuizQuestion("ইন্টারভিউ রুমে ঢুকে প্রথমে কী বলবেন?", listOf("May I come in? Good morning.", "Give me the job.", "Sit down."), 0, "অনুমতি নিয়ে অভিবাদন জানানো ভদ্রতার লক্ষণ।"),
                LessonQuizQuestion("'পরিশ্রমী'-এর ইংরেজি কোনটি?", listOf("Lazy", "Hardworking", "Angry"), 1, "Hardworking অর্থ পরিশ্রমী।"),
                LessonQuizQuestion("'Experience' শব্দের অর্থ কী?", listOf("অভিজ্ঞতা", "বেতন", "ছুটি"), 0, "Experience অর্থ অভিজ্ঞতা।")
            )
        ),
        lessonEntity(
            id = 20,
            titleEn = "Everyday Speaking Practice",
            titleBn = "দৈনন্দিন ইংরেজি চর্চা",
            category = "Conversation",
            levelTag = "Step 20",
            minutes = 10,
            summaryBn = "প্রতিদিন ছোট ছোট বাক্যে চিন্তা করে ভয় কাটিয়ে স্বতঃস্ফূর্ত ইংরেজি বলার কৌশল।",
            explanationBn = "অভিনন্দন! আপনি ২০তম লেসনে পৌঁছেছেন। ইংরেজি বলার সবচেয়ে বড় বাধা হলো ভুল করার ভয়। মনে রাখবেন: ভুল হওয়া স্বাভাবিক! প্রতিদিন নিজের সাথে ৫ মিনিট ছোট ছোট ইংরেজি বাক্য বলুন এবং AI শিক্ষকের সাথে কথা বলুন।",
            examples = listOf(
                LessonExample("Today the weather is very nice.", "আজকের আবহাওয়া খুব সুন্দর।", "টুডে দ্য ওয়েদার ইজ ভেরি নাইস"),
                LessonExample("I am feeling happy today.", "আজ আমার মন খুব ভালো লাগছে।", "আই অ্যাম ফিলিং হ্যাপি টুডে"),
                LessonExample("Let us talk about our plans.", "চলুন আমাদের পরিকল্পনা নিয়ে কথা বলি।", "লেট আস টক অ্যাবাউট আওয়ার প্ল্যানস"),
                LessonExample("Can you explain that word in simple English?", "আপনি কি ওই শব্দটি সহজ ইংরেজিতে বুঝিয়ে দেবেন?", "ক্যান ইউ এক্সপ্লেইন দ্যাট ওয়ার্ড ইন সিম্পল ইংলিশ?"),
                LessonExample("Practice makes me more confident.", "অনুশীলন আমাকে আরও আত্মবিশ্বাসী করে।", "প্র্যাকটিস মেকস মি মোর কনফিডেন্ট")
            ),
            dialogue = listOf(
                DialogueLine("Teacher", "How do you feel about speaking English now?", "এখন ইংরেজি বলতে কেমন লাগছে?"),
                DialogueLine("Student", "I feel much better! I am not afraid anymore.", "আমার এখন অনেক ভালো লাগছে! আমি আর ভয় পাই না।"),
                DialogueLine("Teacher", "I am proud of you! Let us keep practicing together.", "তোমার জন্য গর্ব হচ্ছে! চলো আমরা একসাথে চর্চা চালিয়ে যাই।")
            ),
            quiz = listOf(
                LessonQuizQuestion("ইংরেজি বলার জড়তা কাটানোর সেরা উপায় কী?", listOf("ভয় পেয়ে চুপ থাকা", "প্রতিদিন ছোট বাক্যে কথা বলা", "শুধু কঠিন গ্রামার মুখস্থ করা"), 1, "প্রতিদিন ছোট ছোট বাক্য বলে অনুশীলন করলে জড়তা কাটে।"),
                LessonQuizQuestion("'আত্মবিশ্বাসী'-এর ইংরেজি কী?", listOf("Confident", "Nervous", "Afraid"), 0, "Confident অর্থ আত্মবিশ্বাসী।"),
                LessonQuizQuestion("কথা বলার সময় আটকে গেলে কী করবেন?", listOf("বন্ধ করে দেব", "ধীরে সহজ শব্দে আবার চেষ্টা করব", "অন্য ভাষাও বলব না"), 1, "ধীরে সহজ শব্দে চেষ্টা করাই সঠিক কৌশল।")
            )
        )
    )
}

object BeginnerCurriculumSeed {
    fun allLessons(): List<LessonEntity> = LessonSeedPart1.lessons() + LessonSeedPart2.lessons()
}
