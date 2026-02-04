# Database Connection Troubleshooting

## Sorun
JDBC connection hatası alıyorsunuz: "Unable to acquire JDBC Connection"

## Çözüm Adımları

### 1. Neon Console'dan Connection String Alın

1. Neon Console'a gidin: https://console.neon.tech
2. Projenizi seçin
3. **Connection Details** veya **Connection String** bölümüne gidin
4. **JDBC** formatını seçin
5. Connection string'i kopyalayın

### 2. Connection String Formatı

Neon connection string genellikle şu formatta olur:

```
jdbc:postgresql://[endpoint]/[database]?sslmode=require
```

**Örnek:**
```
jdbc:postgresql://ep-cool-darkness-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
```

### 3. run.sh Dosyasını Güncelleyin

`run.sh` dosyasındaki `DB_URL` değişkenini Neon Console'dan aldığınız connection string ile değiştirin:

```bash
export DB_URL="jdbc:postgresql://YOUR_ENDPOINT/neondb?sslmode=require"
```

### 4. Pooler vs Direct Connection

Neon'da iki tip connection var:

**Pooler (önerilen):**
- Endpoint: `ep-xxx-pooler.region.aws.neon.tech`
- Daha fazla connection desteği
- Production için önerilir

**Direct:**
- Endpoint: `ep-xxx.region.aws.neon.tech` (pooler yok)
- Daha az connection limiti
- Development için uygun

### 5. Test

Uygulamayı yeniden başlatın:
```bash
cd discord-ai-bot-java
./run.sh
```

### 6. Alternatif: Connection String Parametreleri

Eğer hala sorun varsa, connection string'e şu parametreleri ekleyin:

```
jdbc:postgresql://[endpoint]/[database]?sslmode=require&connectTimeout=30&socketTimeout=30
```

## Not

- Connection string'deki endpoint adını doğru yazdığınızdan emin olun
- Database adını (`neondb`) kontrol edin
- Username ve password'ün doğru olduğundan emin olun
- Neon Console'da connection limitlerini kontrol edin

