
package upmc.akka.culto

import math._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.actor.{Props, Actor, ActorRef, ActorSystem}


abstract class ObjectMusical
abstract class SimpleObjectMusical () extends ObjectMusical
abstract class ComposeObjectMusical () extends ObjectMusical
case class Note (pitch:Int, dur:Int, vel:Int) extends SimpleObjectMusical
case class Rest (dur:Int) extends SimpleObjectMusical
case class Sequential (elements:List[ObjectMusical]) extends ComposeObjectMusical
case class Parallel (elements:List[ObjectMusical]) extends ComposeObjectMusical

case class MidiNote (pitch:Int, vel:Int, dur:Int, at:Int) 
  
/////////////////////////////////////////////////

class BachActor extends Actor {

//////////////////////////////////////////////////
 val remote =
  context.actorSelection("akka.tcp://Player@127.0.0.1:6000/user/Instrument")

////////////////////////////////////////////////

def receive = {
    case "START" => {
      println(duration(exemple))
	// play (concat(exemple,exemple))
  play (canon_Bach())
    }
        
}

/////////////////////////////////////////////////

//Question 1

val voixh = Sequential ( List ( 
	Note ( 60, 1000, 100 ) , Note ( 64 , 500 , 108 ) , Note (62, 500,100), 
	Rest (1000) , Note (67, 1000,100)))
val voixb = Sequential ( List ( 
	Note (53 , 2000, 100 ) , Note ( 55, 1000, 108 ) , Note ( 55, 1000,100)))

val exemple = Parallel ( List ( voixh, voixb)) 


//Question 2
 
 // Calcule la duree d'un objet musical
  def duration (obj:ObjectMusical):Int =
  obj match {
    case Note(p,d,v) => d
    case Rest(d) => d
    case Sequential (l) => (l.map(duration)).foldLeft(0)(_+_)
    case Parallel (l) => (l.map(duration)).foldLeft(0)(math.max)  
  }


//Question 3
  def play (obj:ObjectMusical): Unit = {
    play_midi (obj, 0)
  }

  def send_a_note (p:Int, d:Int, v:Int, at:Int): Unit = {
    remote ! MidiNote(p,v,d,at)
  }

  
  def play_midi (obj:ObjectMusical, at:Int): Unit =
  obj match {
    case Note(p,d,v) => send_a_note (p,d,v,at)
    case Rest(d) => Nil
    case Sequential (l) => {var date = at
                        l.foreach(n=>{play_midi(n,date); date = date + duration(n)})}
    case Parallel (l) => l.foreach(n=>play_midi(n,at))
  }
  

 // Copy un objet musical
  def copy (obj:ObjectMusical):ObjectMusical =
  obj match {
    case Note(p,d,v) => Note(p,d,v)
    case Rest(d) => Rest(d)
    case Sequential (l) => Sequential (l.map(copy))
    case Parallel (l) => Parallel (l.map(copy))
  }

  // Compte le nombre de notes d'un objet musical
  def note_count (obj:ObjectMusical):Int =
  obj match {
    case Note(p,d,v) => 1
    case Parallel (l) => (l.map(note_count)).foldLeft(0)(_+_)
    case Sequential (l) => (l.map(note_count)).foldLeft(0)(_+_)
    case _ => 0
  }
 
  // Strech un objet musical par un factor fact
  def stretch (obj:ObjectMusical, fact:Double ):ObjectMusical =
  obj match {
    case Note(p,d,v) => Note(p,(d*fact).toInt,v)
    case Rest(d) => Rest((d*fact).toInt)
    case Parallel (l) => Parallel (l.map(stretch (_,fact)))
    case Sequential (l) => Sequential (l.map(stretch (_,fact)))
  }
 

// Question 4
 
// Transpose obj de n demitons
  def transpose (obj:ObjectMusical, n:Int ):ObjectMusical =
obj match {
  case Note(p,d,v) => Note(p+n,d,v)
  case Rest(d) => Rest(d)
  case Parallel (l) => Parallel (l.map(transpose (_,n)))
  case Sequential (l) => Sequential (l.map(transpose (_,n)))
}

// mirror de obj au tour du center c 
  def mirror (obj:ObjectMusical, c:Int ):ObjectMusical =
obj match {
  case Note(p,d,v) => Note(c - (p - c),d,v)
  case Rest(d) => Rest(d)
  case Parallel (l) => Parallel (l.map(mirror (_,c)))
  case Sequential (l) => Sequential (l.map(mirror (_,c)))
}

// retrograde un obj  
  def retrograde (obj:ObjectMusical):ObjectMusical =
obj match {
  case Sequential (l) => Sequential (l.reverse.map(retrograde))
  case o => o
}

//Question 5

// make a sequential avec n fois obj  
def repeat (obj:ObjectMusical, n:Int):ObjectMusical =
    if (n == 0){obj}
    else{ Sequential(repeatRec(List(obj),n-1 ) )}

def repeatRec(list: List[ObjectMusical], n:Int): List[ObjectMusical]=
  if (n == 0){list}
  else { repeatRec(copy(list.head)::list,n-1)}
      


// make obj en parallele avec lui meme avec un decalage de n ms.
  def canon (obj:ObjectMusical, n:Int):ObjectMusical =
    Parallel(List(obj, Sequential(List(Rest(n),obj))))


//  Met obj1 et obj2 en seqeunce 
  def concat (obj1:ObjectMusical, obj2:ObjectMusical):ObjectMusical =
      Sequential(List(obj1,obj2))


//Question 5 BACH
 val voix1 = Sequential ( List (
  Note (60 , 750 , 106 ) , Note (62 , 250 , 108 ) , Note (63 , 250 , 108 ) , 
  Note (64 , 250 , 109 ) , Note (65 , 250 , 109 ) , Note (66 , 250 , 110 ) , 
  Note (67 , 1000 , 110 ) , Note (68 , 625 , 113 ) ,Note (65 , 125 , 114 ) , 
  Note (61 , 125 , 112 ) , Note (60 , 125 , 112 ) , Note (59 , 500 , 112 ) , 
  Rest (500) , Rest (500) , Note (67 , 1000 , 109 ) ,Note (66 , 1000 , 108 ) , 
  Note (65 , 1000 , 106 ) , Note (64 , 1000 , 106 ) , Note (63 , 1000 , 106 ) , 
  Note (62 , 750 , 106 ) , Note (61 , 250 , 106 ) , Note (58 , 250 , 106 ) , 
  Note (57 , 250 , 106 ) , Note (62 , 500 , 106 ) ,Rest (1000),
  Note (67 , 1000 , 106 ) , Note (65 , 500 , 106 ) , Note (64 , 1000 , 106 )))

val voix2 = Sequential (List (
  Rest (125) , Note (48 , 125 , 100 ), Note (51 , 125 , 100 ),
  Note (55 , 125 , 100 ),Note (60 , 1000 , 100 ),Note (58 , 250 , 100 ), 
  Note (57 , 250 , 100 ),Note (58 , 625 , 100 ),Note (52 , 125 , 100 ),
  Note (50 , 125 , 100 ),Note (52 , 125 , 100 ),Note (53 , 125 , 100 ), 
  Note (48 , 125 , 100 ),Note (53 , 125 , 100 ),Note (55 , 125 , 100 ),
  Note (56 , 750 , 100 ),Note (56 , 250 , 100 ),Note (55 , 250 , 100 ), 
  Note (53 , 250 , 100 ),Note (51 , 625 , 100 ),Note (51 , 125 , 100 ), 
  Note (53 , 125 , 100 ),Note (51 , 125 , 100 ),Note (50 , 250 , 100 ),
  Note (48 , 250 , 100 ),Note (49 , 500 , 100 ), Rest (250) ,Note (50 , 250 , 100 ), 
  Note (51 , 250 , 100 ),Note (50 , 250 , 100 ),Note (48 , 125 , 100 ),
  Note (47 , 125 , 100 ),Note (48 , 125 , 100 ),Note (47 , 125 , 100 ), 
  Note (48 , 125 , 100 ),Note (50 , 125 , 100 ),Note (48 , 125 , 100 ),
  Note (46 , 125 , 100 ),Note (45 , 125 , 100 ),Note (43 , 125 , 100 ), 
  Note (45 , 125 , 100 ),Note (46 , 125 , 100 ),Note (48 , 125 , 100 ),
  Note (45 , 125 , 100 ),Note (46 , 125 , 100 ),Note (48 , 125 , 100 ), 
  Note (50 , 250 , 100 ),Note (60 , 500 , 100 ),Note (58 , 125 , 100 ), 
  Note (57 , 125 , 100 ),Note (58 , 250 , 100 ),Note (55 , 250 , 100 ),
  Note (52 , 250 , 100 ),Note (57 , 125 , 100 ),Note (55 , 125 , 100 ), 
  Note (54 , 250 , 100 ),Note (55 , 125 , 100 ),Note (57 , 125 , 100 ),
  Note (58 , 250 , 100 ), Note (49 , 250 , 100 ),Note (50 , 500 , 100 ), 
  Rest (500) , Rest (250) , Note (50 , 375 , 100 ),Note (53 , 125 , 100 ),
  Note (52 , 125 , 100 ),
  Note (50 , 125 , 100 ),Note (49 , 125 , 100 ),Note (50 , 125 , 100 ), 
  Note (52 , 125 , 100 ),Note (53 , 125 , 100 ),Note (55 , 125 , 100 ),
  Note (58 , 125 , 100 ),Note (57 , 125 , 100 ),Note (55 , 125 , 100 )))

// BON, je pense que j'ai pas compris un aspect de la consigne de la dernière question.
// j'ai essayé d'appliquer les bonnes transposées mais si vous lancez cette methode ça vous fera mal 
// aux oreilles ^^'
// def canon_Bach ():ObjectMusical = {
//     Parallel(
//     List(
//       voix1, transpose(voix1,2),transpose(voix1,4),transpose(voix1,6),transpose(voix1,8),transpose(voix1,10),transpose(voix1,12),
//       Parallel(List( 
//         Sequential(List(voix2, transpose(voix2,2), transpose(voix2,4),transpose(voix2,6),transpose(voix2,8),transpose(voix2,10),transpose(voix2,12))),
//         Sequential(List(Rest(2),transpose(voix2,7), transpose(voix2,14),transpose(voix2,21),transpose(voix2,28),transpose(voix2,35),transpose(voix2,42))) 
                  
//               )
//     ))
//     )}}



// Cette methode est plus 'ecoutable' mais ne respecte pas les transposés... 
def canon_Bach ():ObjectMusical = {
    Parallel(
    List(
      transpose(repeat(voix1,6),2),
      Parallel(List( 
        transpose(repeat(voix2,6),1),
        Sequential(List(Rest(2),transpose(repeat(voix2,6),7))) 
                  )
              )
    ))
  }
}
//////////////////////////////////////////////////


/**************** MAin object *******************/
object bach extends App {
  val system = ActorSystem("Bach")
  val localActor = system.actorOf(Props[BachActor], name = "Bach")
  localActor ! "START"
}

