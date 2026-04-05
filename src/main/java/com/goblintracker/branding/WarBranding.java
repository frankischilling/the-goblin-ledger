package com.goblintracker.branding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WarBranding
{
	public static final String PLUGIN_NAME = "The Goblin Ledger";
	public static final String PLUGIN_TAGLINE = "Big High War God count every goblin.";
	public static final int GOAL_KILLS = 1_000_000;

	public static final String SIDEBAR_TOOLTIP = "The Goblin Ledger";
	public static final String OVERLAY_SESSION_LABEL = "Day";
	public static final String OVERLAY_LIFETIME_LABEL = "Ledger";
	public static final String OVERLAY_TRIP_LABEL = "March";
	public static final String OVERLAY_RATE_LABEL = "War/hr";
	private static final int MILESTONE_PROGRESS_BAR_WIDTH = 20;
	private static final int CAMPAIGN_PROGRESS_BAR_WIDTH = 24;

	private static final int[] MILESTONE_TARGETS = {
		100,
		1_000,
		5_000,
		10_000,
		50_000,
		100_000,
		250_000,
		500_000,
		750_000,
		900_000,
		GOAL_KILLS
	};

	private static final String[] FLAVOR_LINES = {
		"In first war no army stand for Big High War God.",
		"Small weak goblins say yes, and war-book begin.",
		"Big High War God make goblins hard in skin and spirit.",
		"Twelve tribes march when command is spoken.",
		"Commandment says: do not run, do not spare, do not doubt.",
		"When gods leave world, goblins still fight.",
		"Mud plain drank many brave tribe names.",
		"Hopespear dream says tribes must not kill tribes.",
		"Prophecy says new Commander comes and all Gielinor falls.",
		"Bronze throne waits for final victory."
	};

	private static final String[] TAGLINES = {
		"War-book open. Count never sleeps.",
		"Goblin made for war. Ledger made for truth.",
		"Tribe by tribe, mark by mark, million by million.",
		"Big High War God sees each line we write.",
		"No tribe war. All spears point outward.",
		"Mud-plain oath still lives.",
		"New Commander must arrive to finished count.",
		"Victory line gets closer."
	};

	private static final String[] DIRECTIVES = {
		"Always slay enemies of Big High War God.",
		"Do not run from battle.",
		"Do not show mercy to war enemies.",
		"Do not doubt command.",
		"Do not make own plan; follow war-book."
	};

	private static final String BRONZE_COUNT_LORE = String.join("\n\n",
		"THE BRONZE COUNT",
		"A Goblin Chronicle of War, Prophecy, and the Million Dead",
		"In the first age, when the world was younger and the mountains still remembered the heat of their making, the gods went to war. Not in secret, not by whispers, not by hidden knives in the dark, but openly, with banners, storms, beasts, flame, steel, and prayer. The sky shook with their hatred. Valleys were torn into graves before their grass had ever known peace. Rivers ran with mud, ash, and blood so thick that fish died choking in it. Every god sought armies. Every god wanted hands to carry spears and voices to scream their names upon the battlefield.",
		"The God of Shining Light found followers among the proud and disciplined. The God of Dark Fire found those who loved ruin, power, and fear. Other gods found servants among stranger things still--creatures of fang, claw, scale, and sorcery. But the Big High War God had no army. His rage was vast, his hunger for conquest deeper than any mine, yet no people would take his mark. The beardy-short-people refused him. The demons refused him. The tall people with keen blades refused him. He went east and west, north and south, over broken hills and drowned plains, past forests that would later be burned and deserts that would later swallow bones, asking again and again for soldiers, and again and again hearing no.",
		"Then the goblins answered.",
		"They were not mighty then. They were not the hard little killers that later ages would come to know. Their backs were bent. Their skin was soft. Their courage fluttered like a weak flame in bad wind. They lived in scraps and fear, hiding from larger things, fighting over crusts, bones, and broken iron. Yet when the Big High War God came before them and the earth trembled beneath his step, they did not flee. They lifted their ugly little faces and answered with the only thing that had ever truly lived in them.",
		"They said: We fight for you.",
		"That was enough.",
		"The Big High War God took the goblins and remade them. He trained them in battle until their arms stopped shaking. He hammered fury into their hearts. He taught them that fear was filth. He gave them armor to turn aside hurt, weapons to tear open flesh, drums to carry command, banners to gather beneath, and chiefs to drive them forward. He broke them apart into twelve tribes so that each tribe would harden itself in its own way, then bound them together beneath his will so that when the horn of war sounded they would fall upon the world like a plague of knives.",
		"And they were glorious.",
		"Where others fought for land, goblins fought because fighting was breath, hunger, and truth. Where others needed walls, goblins needed only an enemy. Where others wavered, goblins swarmed. They died in heaps and kept coming. They screamed when they struck, laughed when they bled, and bit when their hands were empty. They were small, but they covered battlefields like locusts cover fields. They were crude, but crude things can still kill. Soon the other gods saw what the Big High War God had made and burned with envy.",
		"They wanted goblins too.",
		"So some tribes were sold, traded, given over, or lured away. One tribe to one god, another to another, until goblin blood was spilled beneath many banners. Yet the first making was never forgotten. However far tribes wandered, however many masters claimed them, the oldest truth remained buried inside goblinkind like a shard of bronze in old flesh: they had been made for war by the Big High War God, and all other loyalties were only lesser echoes.",
		"To keep them sharp, the Big High War God gave commandments. Not the soft sort that can be bargained with, not laws bent by excuses or tears, but iron laws. Slay the enemy. Do not run. Show no mercy. Do not doubt. Do not think beyond command. A goblin who broke these laws was no goblin worth keeping. The coward deserved death. The merciful deserved death. The doubter deserved death. The one who made plans apart from command deserved death. In this way he cut away all weakness, until what remained was obedience, savagery, and devotion sharpened into one edge.",
		"For many lifetimes the wars of gods lasted. Countless goblins died in them and counted that dying no loss, because to die in battle was to be used well. But no war lasts forever, not even among gods. In the end the gods withdrew from the world, leaving behind broken lands, broken peoples, and armies that no longer had divine voices in their ears. The goblins remained what they had been made to be: soldiers with no commander, weapons with no hand upon the hilt.",
		"So they kept fighting.",
		"They fought the tall people with keen blades. They fought the ones behind stone walls. They fought those who would not come out and meet them honorably. They fought for scraps. They fought for pride. They fought because their blood knew no other trade. Yet without the clear will of the Big High War God, battle turned inward. Tribe struck tribe. Banner struck banner. Old grudges became new wars. Hunger gnawed. Command splintered. A people forged for conquest began to spend itself on its own flesh.",
		"Then came the great slaughter upon the Plain of Mud.",
		"All tribes met there. The ground was already soft before the battle began, but by the end it had become a red swamp churned by feet, bodies, and iron. Goblins killed goblins for days without count. Standards fell. War-drums split. Chiefs were hacked apart in sight of their own warriors. Mud drank eyes, teeth, fingers, and spearheads. By the time the sun had crossed the sky enough times for memory to blur, so many corpses lay across the plain that the living stumbled over the dead as if climbing hills made of their own kin.",
		"It might have ended there. Goblinkind might have buried itself beneath its own rage.",
		"But on one night, while the fires smoldered low and the wounded muttered or died, Hopespear of the Narogoshuun saw a vision. The Big High War God came to him not as soft comfort, but as iron certainty. Hopespear gathered the leaders of the tribes and spoke what had been shown.",
		"Too much battle, said the vision, would end all goblins. Goblins must not cease war, for war was their making, but goblins must cease wasting war upon goblins. Tribe must not destroy tribe. Goblins must turn their violence outward again and await the day a new Commander would come. That Commander would gather goblinkind, conquer all of Gielinor, humble every race and every god, and prepare the throne of bronze for the return of the Big High War God. Then war would end not in exhaustion, nor in confusion, but in victory that lasted forever.",
		"So the tribes ceased that one battle, though not their nature. Upon the Plain of Mud they raised a temple of rough hands and stubborn faith. There they offered sacrifice. There they kept the prophecy. There they waited.",
		"Centuries passed.",
		"Empires rose like proud towers and fell like rotten scaffolds. Human kings were crowned and buried. Dwarves cut deeper halls. Elves kept their distance behind old beauty and older sorrow. Demons whispered. Priests lied. Merchants fattened. Adventurers came crawling out of nowhere like a new kind of vermin, all bright gear and greedy eyes, poking into caves, tombs, ruins, camps, strongholds, and sacred places with the same smirk on their face and the same appetite in their hands. They killed for coin, for contracts, for trophies, for tasks, for amusement, for habit. They made slaughter mundane.",
		"To most goblins, these adventurers were merely another enemy race: tall, arrogant, impossible to understand, always barging in. But among the old-minded, the shamans, the scar-counting veterans, and the keepers of ugly temple memory, another fear began to spread.",
		"For among the adventurers there appeared, from age to age, a stranger sort.",
		"Not the raider. Not the mercenary. Not the questing fool.",
		"The Counter.",
		"The one who did not merely kill goblins, but counted them.",
		"At first the tales were dismissed. Goblins are superstitious by habit and dramatic by instinct. Any warrior who survives long enough becomes a storyteller, and every storyteller fattens truth like a cooking pot. But the stories kept returning from different camps, caves, villages, and ruins. A lone slayer would come. Goblins would die. That was ordinary. Yet after each death, the slayer would pause, glance toward empty air, and seem satisfied by something unseen. Then they would continue. No looting of scraps. No shouting of challenge. No obvious purpose beyond the next kill. Again and again, always with that same rhythm. Strike. Death. Count. Strike. Death. Count.",
		"Some goblins swore they had seen numbers in the air, thin as ghost-light and brief as sparks. Some claimed an invisible bronze tablet hovered beside the killer. Others said a spirit perched at the warrior's shoulder, scratching marks onto an unseen ledger with a nail of iron. A few shamans gave the phenomenon a name: the Bronze Count.",
		"In their telling, the Bronze Count was not a simple record. It was a sign that the prophecy had bent in a direction none had expected. Not the rise of a goblin commander, but the rise of a great enemy commander whose purpose was to test goblinkind through endless death. For if a race is made for war, then counting its dead is another way of mastering it. To know a people one corpse at a time is a dark kind of dominion.",
		"This belief spread fastest among the weakest tribes, for the weak always become theologians when they cannot become victors. Yet even hard tribes began to listen when the milestones appeared.",
		"The first hundred dead meant nothing. Goblins die in hundreds every day under careless boots and steel. The first thousand stirred unease. The first ten thousand became a campfire whisper. The first hundred thousand turned whisper into omen. By then old cave-paintings were being reinterpreted. Old notches on temple posts were being studied. Old chants about the throne of bronze took on a fresh and uglier meaning.",
		"Then one goblin shaman, blind in one eye and burned along the jaw, made a claim that spread from mud-wall to stockade, from mine-tunnel to ruined fort.",
		"He said the promised Commander had indeed come, only not in the form goblins desired.",
		"He said the new Commander was the hand behind the Bronze Count, the one teaching goblins what it meant to be truly seen.",
		"He said every goblin slain beneath that tally was not merely dying, but being offered into a second kind of sacrifice.",
		"He said one million deaths would wake something.",
		"This thought infected goblinkind.",
		"Among some tribes it caused terror. Mothers dragged their young deeper into tunnels. Watch-goblins nailed charms of bent nails and dog teeth over entrances. Patrols were doubled. Drums were quieted at dusk so that the Counter would not hear where they hid. Some camps began smearing mud over banners and armor to avoid notice, as though ugliness could become camouflage against fate itself.",
		"Among other tribes it caused fanaticism.",
		"If one million dead would bring revelation, then death under the Bronze Count could be holy. Warriors painted bronze streaks across their faces and stood in the open, daring the Counter to strike them first. Some chiefs declared that to fall as the ten-thousandth, the hundred-thousandth, or the millionth goblin would make a warrior immortal in tribe-memory. Duels broke out over who had the right to stand nearest the path where the Counter was last seen. Goblins argued over whether being counted erased weakness, whether the tally purified the tribe, whether the millionth death would split the sky and bring back the Big High War God in mail and thunder.",
		"Others, cunning in the miserable goblin way, decided that if the Counter could not be stopped, then the Counter should at least be studied. Scribes of soot and blood began keeping their own records. Crude marks were scratched into cave walls. Bone bundles were tied in groups to mirror reported kills. Captured human traders were shaken until they revealed whatever they knew of adventurers who killed for obsession rather than need. Scouts tracked famous slayers. Old battlefield sites were revisited. Some tribes even tried to create false counts, murdering their own prisoners or rival goblins and pretending those deaths belonged to the Bronze Count, as if prophecy could be padded like numbers on a merchant's ledger.",
		"But the true Count, they believed, could not be cheated.",
		"Because the true Count lived not on wood, bone, parchment, or stone, but in the unseen ledger carried by the enemy. Each goblin death entered it and could not be rubbed out. Session by session, day by day, camp by camp, the number climbed. Goblins began to distinguish between the Little Count and the Long Count. The Little Count was the slaughter of a day, a raid, a cave-clearing, a watch-shift gone wrong. The Long Count was the total burden carried across seasons and years. Shamans argued over which mattered more. Veterans spat and said both mattered if the spear still went in. Yet the terms survived, and in surviving became doctrine.",
		"The Little Count measures the current fury.\nThe Long Count measures destiny.",
		"From this belief came new rites.",
		"When a goblin warband returned from battle, the survivors would sometimes gather around the fire and ask not merely who died, but whether they had been counted. A death by wolf, dwarf, disease, or cave-in was unfortunate. A death by the Counter was portent. Some tribes began carving small bronze circles to wear on string or cord, one for each known counted death in a family line. The uglier and heavier the necklace, the greater the supposed honor. Entire clans built identity around proximity to the tally. One tribe boasted that more of their blood had entered the Long Count than any other, and therefore when the millionth death came the Big High War God would know them first by scent.",
		"Naturally, other tribes beat them with sticks for saying so.",
		"Yet even mockery could not kill the myth. The more goblins died, the more real the Bronze Count became. Each milestone changed the flavor of fear. At fifty thousand, it was a story. At one hundred thousand, it was a pattern. At half a million, it was a weather system hanging over goblinkind, dark and impossible to ignore.",
		"The oldest temple records--those few not eaten, burned, stolen, or peed on--spoke of the throne of bronze. This too shifted under the shadow of the tally. The priests of the Count said that the throne was not merely a seat waiting somewhere in the beyond, but something being built by number. Each counted goblin death laid another invisible plate upon it. Another rivet. Another step. Another armrest hammered into shape by repetition, obsession, and blood. When one million goblins had been entered into the unseen ledger, the throne would at last be complete.",
		"What would sit upon it differed by telling.",
		"Some believed the Big High War God himself would return and at last sort the strong from the weak with proper enthusiasm.",
		"Some believed the millionth goblin would rise again as a bronze king, swollen with all the fury of those counted before him.",
		"Some believed the Counter would be transformed, no longer merely a tall slayer but the executioner-chronicler of a new age, wearing goblin death as a crown.",
		"Some whispered the darkest interpretation of all: that the prophecy had always been upside down in goblin mouths, misunderstood by a people too battle-drunk to hear it properly, and that the new Commander was never meant to lead goblins, but to lead the end of goblins.",
		"This final belief was hated, which gave it power.",
		"It lingered in the corners of temples. It sat beside old warriors who no longer laughed. It made shamans wake sweating from dreams of bronze doors opening inward.",
		"Still the killing continued.",
		"Because prophecy does not stop arrows.\nBecause faith does not blunt swords.\nBecause adventurers do not care what the vermin they kill think about destiny.",
		"And so the Count climbed.",
		"Somewhere, in field, cave, ruin, village, mine, fortress, camp, or nameless patch of dirt, another goblin falls. Somewhere an unseen mark is made. Somewhere a little panel, a tiny box, a cursed square of certainty, shows a number turning higher. To the tallfolk it may be pastime, completion, challenge, joke, or goal. To goblinkind it has become a scripture written in casualties.",
		"They speak now of the Final Goblin.",
		"No goblin agrees what makes him final. The last to die? The chosen one? The luckless fool standing in the wrong square at the wrong moment? A chief? A child? A champion in bronze paint? A nobody hit while carrying a cabbage? Prophecy is cruel that way. It gives importance while refusing dignity. Any goblin may be the last. Every goblin may dream it will not be him.",
		"Yet all know this: when the Long Count reaches one million, something in the world of goblins will change forever. Whether it becomes triumph, revelation, or annihilation, none can say. But the old book said goblins were born of war, made by command, preserved by prophecy, and destined for a greater reckoning beneath bronze.",
		"Now the reckoning has a number.",
		"Now the number has a witness.",
		"Now every goblin camp lives beneath a sky that feels one mark narrower than it did yesterday.",
		"And in the dark, where firelight licks shield rims and nervous hands clutch bent spears, the oldest question still mutters from tusked mouths:",
		"Is the Counter merely killing goblins--\nor building the throne?");

	private static final List<LoreUnlockEntry> LORE_UNLOCK_ENTRIES = List.of(
		new LoreUnlockEntry(
			100,
			"The First Scratches",
			String.join("\n\n",
				"At one hundred goblins slain, the Count is no longer accident. Somewhere in the ugly dark of a goblin camp, a shaman squints into the fire and sees a shape in the sparks: a tall killer marked by repetition. Goblins do not fear a single death. They do not fear ten. They barely fear fifty. But a hundred begins to feel deliberate. A hundred feels like a hand returning to the same wound.",
				"Among the tribes, whispers start. They say a new hunter walks the mud roads and cave-mouth paths. They say this one does not kill for food, land, revenge, or even sport. This one kills and keeps count. The old goblins mutter that numbers matter to tallfolk in dangerous ways. A goblin dies, and somewhere unseen, a mark is carved.",
				"The younger goblins laugh it off. The veterans do not. They have seen patterns become disasters before."),
			"The First Scratches",
			"The Bronze Count has begun. Goblins are starting to notice the pattern."),
		new LoreUnlockEntry(
			1_000,
			"The Campfire Warning",
			String.join("\n\n",
				"At one thousand kills, the story is no longer local. It has spread beyond one cave, one field, one cooking fire. Goblins in different regions now tell the same tale without ever meeting: a relentless slayer, a silent tally, a growing doom. When stories travel faster than goblins themselves, something old is moving beneath them.",
				"The shamans begin to argue over what this means. Some say the killer is merely a blood-drunk adventurer with too much time and too little sanity. Others claim the Count is sacred, that each death is being written into an invisible war-ledger kept for the Big High War God himself. A few whisper a more dangerous idea--that the prophecy of the new Commander has begun, only wrong, twisted, wearing an enemy's face.",
				"In goblin camps, mothers pull their little ones closer at night. Warriors boast louder than before, which is how goblins hide fear. Watchmen stare longer into the dark. Every snapped twig beyond the torchlight feels like a visitor who has come back for another mark.",
				"A thousand dead goblins is not a battle. It is a message."),
			"The Campfire Warning",
			"Your name has become a warning spoken over goblin fires."),
		new LoreUnlockEntry(
			10_000,
			"The Tenth Banner Falls",
			String.join("\n\n",
				"Ten thousand kills turns fear into doctrine. This is the point where the goblins stop treating the Count as rumor and begin shaping belief around it. Crude carvings appear on cave walls: a tall figure beside a column of marks, a bronze eye above a heap of bodies, a throne made from circles and bones. Different tribes draw it differently, but the meaning is always the same. The Count is real. The Count is growing. The Count is meant for something.",
				"The old stories of the twelve tribes begin to stir again. Goblin historians, if such miserable creatures may be called historians, argue that ten thousand is the weight of a fallen banner-host. Enough dead to stain tribal memory. Enough dead to found a legend that will outlive chiefs and huts and whole bloodlines.",
				"Some goblins now believe being slain by the Counter is a kind of destiny. Others flee from any sign of adventurers. A few mad tribes even send their warriors forward on purpose, hoping to be included in the Long Count and remembered when the bronze throne is finally raised. Goblins have always been brutal, but now they have become religious about their brutality.",
				"Ten thousand kills means the Count has become a world inside goblin thought."),
			"The Tenth Banner Falls",
			"The tribes no longer call it chance. They call it prophecy."),
		new LoreUnlockEntry(
			100_000,
			"The Long Count",
			String.join("\n\n",
				"At one hundred thousand kills, the Count becomes bigger than the lives inside it. Goblins born long after the first dead have entered the world already knowing your shadow. Hatchlings hear of the Counter before they hear proper war chants. Crude nursery threats change. Behave, or the tall one who counts will find you. Sleep, or the Bronze Ledger will open. Even goblin humor sours around the number.",
				"The shamans now distinguish between ordinary slaughter and sacred tally. A raid is one thing. A massacre is another. But this--this is the Long Count. This is death stretched into meaning through impossible repetition. Every new goblin corpse is no longer just a body. It is a number added to a structure no goblin fully understands.",
				"Temples to the Big High War God start incorporating the Count into old prophecy. Bent bits of bronze are tied to poles. Bone necklaces are strung in groups of one hundred. Priests smear mud and blood into circular tally shapes across shrine stones. They speak of a throne being built one death at a time, each counted goblin another hammer blow in the forging.",
				"A hundred thousand kills means you are no longer just a slayer. To goblinkind, you are architecture."),
			"The Long Count",
			"Your slaughter has become a scripture. Goblins now measure doom in your numbers."),
		new LoreUnlockEntry(
			500_000,
			"Halfway to the Throne",
			String.join("\n\n",
				"At half a million kills, dread reaches its mature form. No goblin can dismiss the Bronze Count now, not even the stupid ones, and there are many stupid goblins. The number is too large, too obscene, too deliberate. Whole tribes have likely risen and vanished beneath it. Camps that once stood proud are now memory, rumor, or ash. The Count has outlived chiefs, wars, migrations, and petty goblin feuds. It remains.",
				"This is where the theology splits for good.",
				"One sect says the millionth death will herald the return of the Big High War God, who will reward the counted dead by grinding their enemies into the earth and crowning goblinkind over all races at last. Another sect says the millionth death will awaken the Final Goblin, a warrior-king made from the fury of every goblin who has fallen beneath the tally. A darker sect, spoken of only in low voices, claims the truth is much worse: that the Counter is not testing goblins for glory but preparing the end of goblins entirely.",
				"Halfway to a million, every interpretation feels possible and every interpretation feels terrible.",
				"Some tribes become fanatical and paint themselves bronze before battle, trying to look worthy of being counted. Others bury their banners and hide underground. Still others try to preserve records of the dead, desperate that if goblins must vanish, they will at least vanish remembered. The Count has turned goblins against fate itself, and that may be the cruelest battle of all.",
				"Half a million kills means the throne is no longer theory. In the goblin mind, it is half-built."),
			"Halfway to the Throne",
			"The Bronze Count has grown monstrous. Goblins believe the throne is already rising."),
		new LoreUnlockEntry(
			GOAL_KILLS,
			"The Bronze Throne",
			String.join("\n\n",
				"One million goblins dead. The number has passed beyond slaughter, beyond obsession, beyond any ordinary shape of violence. It has entered myth complete. If the earlier milestones were warnings, scriptures, and prophecies, this is fulfillment. Somewhere in the spiritual wreckage of goblinkind, something has finally clicked into place like the last bronze plate hammered into a waiting seat.",
				"Every goblin belief collides here.",
				"The faithful cry that the throne is finished and the Big High War God will soon return to claim it. The zealots say the millionth goblin has become sacred, risen above all tribes, all banners, all petty camp rivalries. The fearful insist that the prophecy was misunderstood from the beginning and that the new Commander was never meant to lead goblins, but to master their extinction through perfect counting. The oldest shamans, those bent nearly double with age and memory, say only this: no race can be counted to one million by the same enemy without becoming bound to that enemy forever.",
				"In the final telling, your name is no longer spoken as a person. It becomes a title. The Counter. The Bronze Hand. The Tally-Maker. The Commander of the Last War. Goblin children who survive will inherit stories of you as if you were storm, plague, famine, and god all mashed into one terrible shape. Some will hate you. Some will worship you. Some will refuse to believe anyone real could have done it.",
				"But the dead remain dead, and the number remains true.",
				"One million kills means the ledger is full. The goblins have been written into your myth, and you into theirs. Whatever the prophecy truly meant, the Bronze Count has reached its final form.",
				"The throne is waiting."),
			"The Bronze Throne",
			"One million goblins have entered the Long Count. The prophecy stands complete."));

	private WarBranding()
	{
	}

	public static String tabCampaignLabel()
	{
		return "War Book";
	}

	public static String tabFrontsLabel()
	{
		return "Tribes";
	}

	public static String tabSpoilsLabel()
	{
		return "Spoils";
	}

	public static String tabChronicleLabel()
	{
		return "Chronicle";
	}

	public static String tabCanonBookLabel()
	{
		return "Canon Book";
	}

	public static String tabLoreReaderLabel()
	{
		return "Lore Reader";
	}

	public static String bronzeCountLoreText()
	{
		return BRONZE_COUNT_LORE;
	}

	public static String bronzeCountCanonBookText()
	{
		String text = BRONZE_COUNT_LORE;
		text = text.replace(
			"THE BRONZE COUNT\n\nA Goblin Chronicle of War, Prophecy, and the Million Dead",
			"THE BRONZE COUNT\nA Goblin Chronicle of War, Prophecy, and the Million Dead\n\n========================================");
		text = insertBookSection(text,
			"In the first age, when the world was younger and the mountains still remembered the heat of their making, the gods went to war. Not in secret, not by whispers, not by hidden knives in the dark, but openly, with banners, storms, beasts, flame, steel, and prayer. The sky shook with their hatred. Valleys were torn into graves before their grass had ever known peace. Rivers ran with mud, ash, and blood so thick that fish died choking in it. Every god sought armies. Every god wanted hands to carry spears and voices to scream their names upon the battlefield.",
			"I. First Age of War");
		text = insertBookSection(text,
			"Then came the great slaughter upon the Plain of Mud.",
			"II. The Plain of Mud");
		text = insertBookSection(text,
			"Centuries passed.",
			"III. The Long Waiting");
		text = insertBookSection(text,
			"For among the adventurers there appeared, from age to age, a stranger sort.",
			"IV. The Counter Appears");
		text = insertBookSection(text,
			"Then one goblin shaman, blind in one eye and burned along the jaw, made a claim that spread from mud-wall to stockade, from mine-tunnel to ruined fort.",
			"V. Prophecy Bent Against the Tribes");
		text = insertBookSection(text,
			"From this belief came new rites.",
			"VI. Rites of Number and Blood");
		text = insertBookSection(text,
			"The oldest temple records--those few not eaten, burned, stolen, or peed on--spoke of the throne of bronze. This too shifted under the shadow of the tally. The priests of the Count said that the throne was not merely a seat waiting somewhere in the beyond, but something being built by number. Each counted goblin death laid another invisible plate upon it. Another rivet. Another step. Another armrest hammered into shape by repetition, obsession, and blood. When one million goblins had been entered into the unseen ledger, the throne would at last be complete.",
			"VII. The Throne of Bronze");
		text = insertBookSection(text,
			"Still the killing continued.",
			"VIII. The Last Question");

		text = text.replace("They said: We fight for you.", "\"We fight for you.\"");
		text = text.replace("That was enough.", "\"That was enough.\"");
		text = text.replace(
			"The Little Count measures the current fury.\nThe Long Count measures destiny.",
			"\"The Little Count measures the current fury.\"\n\"The Long Count measures destiny.\"");
		return text;
	}

	public static List<LoreUnlockEntry> loreUnlockEntries()
	{
		return LORE_UNLOCK_ENTRIES;
	}

	public static List<String> generalKillPopups()
	{
		return WarTextPack.generalKillPopups();
	}

	public static List<String> rareOmens()
	{
		return WarTextPack.rareOmens();
	}

	public static List<String> ambientLoreLines()
	{
		return WarTextPack.ambientLore();
	}

	public static List<String> sessionStartLines()
	{
		return WarTextPack.sessionStartLines();
	}

	public static List<String> sessionEndLines()
	{
		return WarTextPack.sessionEndLines();
	}

	public static List<String> rankNames()
	{
		return WarTextPack.rankNames();
	}

	public static List<String> milestoneTitlesPack()
	{
		return WarTextPack.milestoneTitles();
	}

	public static String milestoneBurstLine(int milestone)
	{
		return WarTextPack.milestoneBurstLine(milestone);
	}

	public static String phaseNameForKills(int kills)
	{
		return WarTextPack.phaseNameForKills(kills);
	}

	private static String insertBookSection(String text, String marker, String heading)
	{
		String section = "----------------------------------------\n"
			+ heading
			+ "\n----------------------------------------\n\n"
			+ marker;
		return text.replace(marker, section);
	}

	public static String overviewSessionLabel()
	{
		return "Kills this day: ";
	}

	public static String overviewTripLabel()
	{
		return "Kills this march: ";
	}

	public static String overviewLifetimeLabel()
	{
		return "Kills in war-book: ";
	}

	public static String overviewRateLabel()
	{
		return "War speed each hour: ";
	}

	public static String overviewCompletionLabel()
	{
		return "Great war completion: ";
	}

	public static String overviewRemainingLabel()
	{
		return "Enemies still standing: ";
	}

	public static String overviewMilestoneProgressLabel()
	{
		return "Prophecy mark progress: ";
	}

	public static String overviewMilestoneEtaLabel()
	{
		return "Next prophecy ETA: ";
	}

	public static String overviewProfileLabel()
	{
		return "Commander on duty: ";
	}

	public static String overviewProfileNoneLabel()
	{
		return "No commander";
	}

	public static String overviewTitleLabel()
	{
		return "War title: ";
	}

	public static String overviewTaglineLabel()
	{
		return "War saying: ";
	}

	public static String overviewCampaignProgressLabel()
	{
		return "Million-mark progress: ";
	}

	public static String overviewProjectionLabel()
	{
		return "When war ends: ";
	}

	public static String overviewDirectiveLabel()
	{
		return "Commandment: ";
	}

	public static String overviewOverallWritingLabel()
	{
		return "War-book oath: ";
	}

	public static String overviewFlavorLabel()
	{
		return "Goblin book line: ";
	}

	public static String overviewMilestonesLabel()
	{
		return "Prophecy marks reached:";
	}

	public static String overviewNoMilestonesLabel()
	{
		return "No prophecy marks reached yet.";
	}

	public static String overviewNextTargetLabel()
	{
		return "Next prophecy mark: ";
	}

	public static String emptyAreasText()
	{
		return "No tribe front has kills written yet.";
	}

	public static String emptyLootText()
	{
		return "No spoils written in war-book yet.";
	}

	public static String emptyHistoryText()
	{
		return "No battle lines written yet.";
	}

	public static String completionText(int lifetimeKills)
	{
		double boundedKills = Math.max(0, lifetimeKills);
		double completion = Math.min(100.0D, (boundedKills * 100.0D) / GOAL_KILLS);
		return String.format(Locale.US, "%.4f%%", completion);
	}

	public static int hostilesRemaining(int lifetimeKills)
	{
		return Math.max(0, GOAL_KILLS - Math.max(0, lifetimeKills));
	}

	public static String flavorLine(int lifetimeKills, int stride)
	{
		return pickRotatingLine(WarTextPack.ambientLore(), lifetimeKills, stride);
	}

	public static String rotatingTagline(int lifetimeKills, int stride)
	{
		return pickRotatingLine(WarTextPack.shortSlogans(), lifetimeKills, stride);
	}

	public static String overallWriting(int lifetimeKills, int stride)
	{
		String tagline = rotatingTagline(lifetimeKills, stride);
		String directive = campaignDirective(lifetimeKills);
		return tagline + " | " + directive;
	}

	public static String campaignDirective(int lifetimeKills)
	{
		return pickRotatingLine(WarTextPack.bandosWarLines(), lifetimeKills, 5000);
	}

	public static String operativeTitle(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		if (boundedKills >= GOAL_KILLS)
		{
			return "Bronze Throne Commander";
		}
		if (boundedKills >= 900000)
		{
			return "Near-Return Herald";
		}
		if (boundedKills >= 500000)
		{
			return "Mud-Plain Marshal";
		}
		if (boundedKills >= 100000)
		{
			return "Temple War Captain";
		}
		if (boundedKills >= 10000)
		{
			return "Tribe Breaker";
		}
		if (boundedKills >= 1000)
		{
			return "Hopespear Keeper";
		}
		if (boundedKills >= 100)
		{
			return "First Spear";
		}

		return "New War Scribe";
	}

	public static String milestoneMessage(int lifetimeKills)
	{
		String title = milestoneTitle(lifetimeKills);
		String burst = WarTextPack.milestoneBurstLine(lifetimeKills);
		if (burst == null || burst.isBlank())
		{
			return "Prophecy mark reached: " + title + " (" + lifetimeKills + "). Keep writing war-book.";
		}

		return "Prophecy mark reached: " + title + " (" + lifetimeKills + "). " + burst;
	}

	private static String pickRotatingLine(List<String> lines, int lifetimeKills, int stride)
	{
		if (lines == null || lines.isEmpty())
		{
			return "";
		}

		int safeStride = Math.max(1, stride);
		int index = Math.floorDiv(Math.max(0, lifetimeKills), safeStride) % lines.size();
		return lines.get(index);
	}

	public static int[] milestoneTargets()
	{
		return MILESTONE_TARGETS.clone();
	}

	public static List<String> unlockedMilestones(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		List<String> unlocked = new ArrayList<>();
		for (int target : MILESTONE_TARGETS)
		{
			if (boundedKills >= target)
			{
				unlocked.add("[x] " + formatWholeNumber(target) + " - " + milestoneTitle(target));
			}
		}

		return unlocked;
	}

	public static String nextMilestoneSummary(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		for (int target : MILESTONE_TARGETS)
		{
			if (boundedKills < target)
			{
				int remaining = target - boundedKills;
				return formatWholeNumber(target) + " - " + milestoneTitle(target)
					+ " (" + formatWholeNumber(remaining) + " remaining)";
			}
		}

		return "All prophecy marks reached.";
	}

	public static String milestoneProgressSummary(int lifetimeKills)
	{
		MilestoneWindow window = resolveMilestoneWindow(lifetimeKills);
		String progressBar = renderProgressBar(window.getProgress(), MILESTONE_PROGRESS_BAR_WIDTH);

		if (window.isCompleted())
		{
			return progressBar + " 100.00% complete";
		}

		return progressBar + " "
			+ String.format(
				Locale.US,
				"%.2f%% toward %s (%s -> %s)",
				window.getProgress() * 100.0D,
				formatWholeNumber(window.getNextTarget()),
				formatWholeNumber(window.getPreviousTarget()),
				formatWholeNumber(window.getNextTarget()));
	}

	public static int milestoneProgressPercent(int lifetimeKills)
	{
		MilestoneWindow window = resolveMilestoneWindow(lifetimeKills);
		return toPercent(window.getProgress());
	}

	public static String milestoneWindowText(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		MilestoneWindow window = resolveMilestoneWindow(boundedKills);
		if (window.isCompleted())
		{
			return "All prophecy marks reached.";
		}

		int remaining = Math.max(0, window.getNextTarget() - boundedKills);
		return formatWholeNumber(window.getPreviousTarget())
			+ " -> " + formatWholeNumber(window.getNextTarget())
			+ " (" + formatWholeNumber(remaining) + " remaining)";
	}

	public static String milestoneEtaSummary(int lifetimeKills, int killsPerHour)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		MilestoneWindow window = resolveMilestoneWindow(boundedKills);
		if (window.isCompleted())
		{
			return "All prophecy marks complete.";
		}

		int remaining = Math.max(0, window.getNextTarget() - boundedKills);
		int safeRate = Math.max(0, killsPerHour);
		if (remaining <= 0)
		{
			return "Mark reached.";
		}

		if (safeRate <= 0)
		{
			return "Unknown. Raise kill speed to read next mark time.";
		}

		long hours = (long) Math.ceil(remaining / (double) safeRate);
		long days = hours / 24;
		long remHours = hours % 24;
		return "~" + days + "d " + remHours + "h to " + formatWholeNumber(window.getNextTarget());
	}

	public static String campaignProgressSummary(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		double progress = safeRatio(boundedKills, GOAL_KILLS);
		String progressBar = renderProgressBar(progress, CAMPAIGN_PROGRESS_BAR_WIDTH);
		return progressBar + " "
			+ String.format(
				Locale.US,
				"%s / %s (%.4f%%)",
				formatWholeNumber(boundedKills),
				formatWholeNumber(GOAL_KILLS),
				progress * 100.0D);
	}

	public static int campaignProgressPercent(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		return toPercent(safeRatio(boundedKills, GOAL_KILLS));
	}

	public static String projectedCompletionSummary(int lifetimeKills, int killsPerHour)
	{
		int remaining = hostilesRemaining(lifetimeKills);
		if (remaining <= 0)
		{
			return "War complete. No goblin hostiles remain.";
		}

		int safeRate = Math.max(0, killsPerHour);
		if (safeRate <= 0)
		{
			return "Unknown. Need kill speed before war end can be read.";
		}

		long hours = (long) Math.ceil(remaining / (double) safeRate);
		long days = hours / 24;
		long remHours = hours % 24;
		return "~" + days + "d " + remHours + "h at current war speed.";
	}

	public static String milestoneTitle(int lifetimeKills)
	{
		switch (lifetimeKills)
		{
			case 100:
				return "First Blood";
			case 1000:
				return "Goblin Bane";
			case 5000:
				return "Village Scourge";
			case 10000:
				return "Green Reaper";
			case 50000:
				return "Campbreaker";
			case 100000:
				return "Goblin Menace";
			case 250000:
				return "The Long War";
			case 500000:
				return "Half the Work";
			case 750000:
				return "No Turning Back";
			case 900000:
				return "The End Nears";
			case GOAL_KILLS:
				return "Extinction Event";
			default:
				return "Threshold Broken";
		}
	}

	private static MilestoneWindow resolveMilestoneWindow(int lifetimeKills)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		int previousTarget = 0;

		for (int target : MILESTONE_TARGETS)
		{
			if (boundedKills < target)
			{
				double progress = safeRatio(boundedKills - previousTarget, target - previousTarget);
				return new MilestoneWindow(previousTarget, target, progress, false);
			}

			previousTarget = target;
		}

		return new MilestoneWindow(GOAL_KILLS, GOAL_KILLS, 1.0D, true);
	}

	private static double safeRatio(int numerator, int denominator)
	{
		if (denominator <= 0)
		{
			return 1.0D;
		}

		double ratio = Math.max(0.0D, numerator) / (double) denominator;
		return Math.max(0.0D, Math.min(1.0D, ratio));
	}

	private static int toPercent(double progress)
	{
		double boundedProgress = Math.max(0.0D, Math.min(1.0D, progress));
		return (int) Math.round(boundedProgress * 100.0D);
	}

	private static String renderProgressBar(double progress, int width)
	{
		int boundedWidth = Math.max(5, width);
		double boundedProgress = Math.max(0.0D, Math.min(1.0D, progress));
		int filled = (int) Math.round(boundedProgress * boundedWidth);
		filled = Math.max(0, Math.min(boundedWidth, filled));
		return "[" + repeat('#', filled) + repeat('-', boundedWidth - filled) + "]";
	}

	private static String repeat(char c, int count)
	{
		int boundedCount = Math.max(0, count);
		StringBuilder builder = new StringBuilder(boundedCount);
		for (int i = 0; i < boundedCount; i++)
		{
			builder.append(c);
		}
		return builder.toString();
	}

	private static String formatWholeNumber(int value)
	{
		return NumberFormat.getIntegerInstance(Locale.US).format(Math.max(0, value));
	}

	private static final class MilestoneWindow
	{
		private final int previousTarget;
		private final int nextTarget;
		private final double progress;
		private final boolean completed;

		private MilestoneWindow(int previousTarget, int nextTarget, double progress, boolean completed)
		{
			this.previousTarget = previousTarget;
			this.nextTarget = nextTarget;
			this.progress = progress;
			this.completed = completed;
		}

		private int getPreviousTarget()
		{
			return previousTarget;
		}

		private int getNextTarget()
		{
			return nextTarget;
		}

		private double getProgress()
		{
			return progress;
		}

		private boolean isCompleted()
		{
			return completed;
		}
	}

	public static final class LoreUnlockEntry
	{
		private final int milestoneKills;
		private final String title;
		private final String body;
		private final String unlockTitle;
		private final String unlockText;

		private LoreUnlockEntry(int milestoneKills, String title, String body, String unlockTitle, String unlockText)
		{
			this.milestoneKills = milestoneKills;
			this.title = title;
			this.body = body;
			this.unlockTitle = unlockTitle;
			this.unlockText = unlockText;
		}

		public int getMilestoneKills()
		{
			return milestoneKills;
		}

		public String getTitle()
		{
			return title;
		}

		public String getUnlockTitle()
		{
			return unlockTitle;
		}

		public String toLoreText()
		{
			return formatWholeNumber(milestoneKills) + " Kills - " + title
				+ "\n\n"
				+ body
				+ "\n\nUnlock text:\n"
				+ unlockTitle
				+ "\n"
				+ unlockText;
		}
	}
}
