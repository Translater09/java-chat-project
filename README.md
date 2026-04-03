Java Channel-Based Wireless Chat System

Bu proje, Java programlama dili kullanılarak geliştirilmiş, TCP/IP soket protokolü üzerinden çalışan bir Halk Bandı Telsiz Simülasyonu uygulamasıdır. Sistem, kullanıcıların yerel ağ (LAN) veya internet üzerinden belirli frekanslarda (kanallarda) birbirleriyle iletişim kurmasına olanak tanır.
+4

👥 Geliştirici Ekibi
Berkay Avcıoğlu - 230201025

Batuhan Hasanoğlu - 220204009

Berkay Çevirici - 220204054

🛠 Teknik Özellikler & Gereksinimler
1. Sistem Mimarisi

Dil: Java 
+1


Protokol: TCP/IP Socket Programming 


Arayüz: Java Swing (Desktop GUI) 
+1


Kanal Yapısı: 1 ile 100 arasında toplam 100 farklı kanal desteği mevcuttur.
+1


Ortak Kanal (Common Channel): Sistem otomatik olarak Kanal 16 üzerinden başlar ve tüm başlangıç iletişimi bu kanalda gerçekleşir.
+1

2. Öne Çıkan Fonksiyonlar

Kanal Değiştirme: Kullanıcılar GUI üzerinden istedikleri kanala (1-100) geçiş yapabilir ve sadece o kanaldaki mesajları görürler.


Kanal Durum Bilgisi: Mevcut kanalların doluluk oranları ve boş olup olmadıkları görüntülenebilir.


Kullanıcı Takibi: Herhangi bir anda hangi kanalda hangi kullanıcıların olduğu listelenebilir.


Bağlantı Limiti: Sistem performansı korumak adına aynı anda maksimum 50 kullanıcıya kadar hizmet verecek şekilde sınırlandırılmıştır.


LAN/Internet Desteği: Uygulama, sunucu IP adresi girilerek yerel ağdaki veya internet üzerindeki farklı bilgisayarlar arasında çalışabilir.

🚀 Nasıl Çalıştırılır?
Sunucuyu Başlatma (Server)
server.Server sınıfını çalıştırın.

Sunucu varsayılan olarak 1234 portunu dinlemeye başlayacaktır.

İstemciyi Başlatma (Client)
client.ChatGUI sınıfını çalıştırın.

Açılan panelde:

Server IP: Sunucunun çalıştığı bilgisayarın IP adresini girin (Aynı bilgisayar için 127.0.0.1).

Username: Kullanıcı adınızı girin.

Connect butonuna basarak telsiz ağına dahil olun.

Kullanım İpuçları
Switch Channel: Farklı bir frekansa geçmek için kanal numarasını girip butona basın.

Channels Info: Hangi kanalların kullanımda olduğunu ve kullanıcı sayılarını raporlar.

Who's Here: Bulunduğunuz kanaldaki diğer kullanıcıların listesini gösterir.

📌 Proje Gereksinim Tablosu
İster No	Açıklama	Durum
1	Java Masaüstü Uygulaması (GUI)	    ✅
2	Ortak Kanal (Kanal 16) Başlangıcı	✅
3	100 Kanal Desteği (1-100)	        ✅
4	Özel Kanalda Sadece O Kanalı Görme	✅
5	Kanal Doluluk/Boşluk Bilgisi	    ✅
6	Maksimum 50 Kullanıcı Sınırı	    ✅
7	LAN ve İnternet Üzerinden Bağlantı	✅
