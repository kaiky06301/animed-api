#!/usr/bin/env bash
#
# Recria o cenário de demonstração do Animed.
#
# O perfil local usa H2 em memória: ao reiniciar a API, os dados cadastrados
# durante os testes são perdidos. Este script recompõe o cenário em segundos,
# pelos próprios endpoints, respeitando as regras de negócio (o veterinário
# registra as vacinas, o tutor recebe os pontos).
#
# Uso:  ./scripts/dados-demo.sh [url-da-api]
#
set -euo pipefail

API="${1:-http://localhost:8080}"
SENHA="animed123"

echo "==> Usando a API em $API"

entrar() {
  curl -s -X POST "$API/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"senha\":\"$SENHA\"}" |
    python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])'
}

TOKEN_TUTOR=$(entrar "tutor@animed.com.br")
TOKEN_DOUTOR=$(entrar "doutor@animed.com.br")
ID_TUTOR=$(curl -s -X POST "$API/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"tutor@animed.com.br\",\"senha\":\"$SENHA\"}" |
  python3 -c 'import sys,json;print(json.load(sys.stdin)["idTutor"])')

echo "==> Tutor de demonstração: id $ID_TUTOR"

# O banco local é um arquivo e sobrevive aos reinícios: sem esta checagem,
# cada execução criaria outro Thor e outra Mel ao lado dos anteriores.
PETS_EXISTENTES=$(curl -s "$API/api/pets/por-tutor/$ID_TUTOR" \
  -H "Authorization: Bearer $TOKEN_TUTOR" |
  python3 -c 'import sys,json;print(json.load(sys.stdin)["totalElements"])')

if [ "$PETS_EXISTENTES" -gt 0 ]; then
  echo
  echo "O cenário já existe: o tutor tem $PETS_EXISTENTES pet(s) cadastrado(s)."
  echo "Para recomeçar do zero, pare a API, apague a pasta data/ e suba de novo:"
  echo
  echo "    rm -rf data/ && ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2"
  echo
  exit 0
fi

criar_pet() {
  curl -s -X POST "$API/api/pets" \
    -H "Authorization: Bearer $TOKEN_TUTOR" \
    -H 'Content-Type: application/json' \
    -d "$1" | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])'
}

ID_THOR=$(criar_pet "{
  \"nome\":\"Thor\",
  \"especie\":\"CACHORRO\",
  \"sexo\":\"MACHO\",
  \"raca\":\"Golden Retriever\",
  \"dataNascimento\":\"2017-03-14\",
  \"pesoKg\":32.5,
  \"castrado\":false,
  \"observacoesSaude\":\"Displasia leve no quadril, acompanhamento semestral\",
  \"idTutor\":$ID_TUTOR
}")
echo "==> Thor criado (id $ID_THOR)"

ID_MEL=$(criar_pet "{
  \"nome\":\"Mel\",
  \"especie\":\"GATO\",
  \"sexo\":\"FEMEA\",
  \"raca\":\"Siamês\",
  \"dataNascimento\":\"2022-06-01\",
  \"pesoKg\":4.1,
  \"castrado\":true,
  \"observacoesSaude\":\"Saudável\",
  \"idTutor\":$ID_TUTOR
}")
echo "==> Mel criada (id $ID_MEL)"

# As vacinas são registradas pelo veterinário, como manda a regra
registrar_vacina() {
  curl -s -o /dev/null -w "%{http_code}" -X POST "$API/api/vacinas" \
    -H "Authorization: Bearer $TOKEN_DOUTOR" \
    -H 'Content-Type: application/json' \
    -d "$1"
}

echo -n "==> Vacina V10 no Thor: "
registrar_vacina "{
  \"nomeVacina\":\"V10 (múltipla canina)\",
  \"dataAplicacao\":\"2026-03-10\",
  \"dataProximaDose\":\"2027-03-10\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"lote\":\"LT-2026-118\",
  \"idPet\":$ID_THOR
}"
echo

echo -n "==> Antirrábica no Thor: "
registrar_vacina "{
  \"nomeVacina\":\"Antirrábica\",
  \"dataAplicacao\":\"2026-06-22\",
  \"dataProximaDose\":\"2027-06-22\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"lote\":\"LT-2026-443\",
  \"idPet\":$ID_THOR
}"
echo

echo -n "==> V8 no Thor (2025): "
registrar_vacina "{
  \"nomeVacina\":\"V8 (óctupla canina)\",
  \"dataAplicacao\":\"2025-09-15\",
  \"dataProximaDose\":\"2026-09-15\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"lote\":\"LT-2025-072\",
  \"idPet\":$ID_THOR
}"
echo

echo -n "==> Giárdia no Thor (2025): "
registrar_vacina "{
  \"nomeVacina\":\"Giárdia\",
  \"dataAplicacao\":\"2025-12-15\",
  \"dataProximaDose\":\"2026-12-15\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"lote\":\"LT-2025-311\",
  \"idPet\":$ID_THOR
}"
echo

# Uma vacina vencida: é o que faz o app mostrar o alerta de atraso
VACINA_ANTIGA=$(date -v-13m +%F 2>/dev/null || date -d "-13 months" +%F)
VENCEU_MES_PASSADO=$(date -v-1m +%F 2>/dev/null || date -d "-1 month" +%F)

echo -n "==> Leishmaniose no Thor (reforço vencido): "
registrar_vacina "{
  \"nomeVacina\":\"Leishmaniose\",
  \"dataAplicacao\":\"$VACINA_ANTIGA\",
  \"dataProximaDose\":\"$VENCEU_MES_PASSADO\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"lote\":\"LT-2025-904\",
  \"idPet\":$ID_THOR
}"
echo

echo -n "==> V3 na Mel: "
registrar_vacina "{
  \"nomeVacina\":\"V3 (tríplice felina)\",
  \"dataAplicacao\":\"2026-05-05\",
  \"dataProximaDose\":\"2027-05-05\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"idPet\":$ID_MEL
}"
echo

# --- Medicamentos -----------------------------------------------------------
# Quem prescreve é o veterinário; ao tutor cabe registrar as doses dadas.
prescrever() {
  curl -s -X POST "$API/api/medicamentos" \
    -H "Authorization: Bearer $TOKEN_DOUTOR" \
    -H 'Content-Type: application/json' \
    -d "$1" | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])'
}

HOJE=$(date +%F)
FIM_ANTIBIOTICO=$(date -v+6d +%F 2>/dev/null || date -d "+6 days" +%F)
INICIO_ANTIGO=$(date -v-20d +%F 2>/dev/null || date -d "-20 days" +%F)
FIM_ANTIGO=$(date -v-13d +%F 2>/dev/null || date -d "-13 days" +%F)

echo
ID_AMOXI=$(prescrever "{
  \"idPet\":$ID_THOR,
  \"nome\":\"Amoxicilina 250mg\",
  \"dosagem\":\"1 comprimido\",
  \"intervaloHoras\":12,
  \"dataInicio\":\"$HOJE\",
  \"dataFim\":\"$FIM_ANTIBIOTICO\",
  \"observacao\":\"Dar junto com a comida, sem partir o comprimido\"
}")
echo "==> Amoxicilina prescrita ao Thor (12/12h por 7 dias)"

prescrever "{
  \"idPet\":$ID_THOR,
  \"nome\":\"Vermífugo (Drontal Plus)\",
  \"dosagem\":\"1 comprimido\",
  \"intervaloHoras\":2160,
  \"dataInicio\":\"$HOJE\",
  \"observacao\":\"Repetir a cada três meses, mesmo sem sinal de verme\"
}" > /dev/null
echo "==> Vermífugo prescrito ao Thor (a cada 3 meses)"

prescrever "{
  \"idPet\":$ID_THOR,
  \"nome\":\"Anti-inflamatório (terminado)\",
  \"dosagem\":\"meio comprimido\",
  \"intervaloHoras\":24,
  \"dataInicio\":\"$INICIO_ANTIGO\",
  \"dataFim\":\"$FIM_ANTIGO\",
  \"observacao\":\"Displasia: usar apenas em crise de dor\"
}" > /dev/null
echo "==> Anti-inflamatório do Thor já venceu, aguardando o tutor confirmar"

prescrever "{
  \"idPet\":$ID_MEL,
  \"nome\":\"Antipulgas (Bravecto)\",
  \"dosagem\":\"1 pipeta\",
  \"intervaloHoras\":720,
  \"dataInicio\":\"$HOJE\",
  \"observacao\":\"Aplicar na nuca, com o pelo separado\"
}" > /dev/null
echo "==> Antipulgas prescrito à Mel (mensal)"

ID_CARDIO=$(prescrever "{
  \"idPet\":$ID_THOR,
  \"nome\":\"Vetmedin 5mg (coração)\",
  \"dosagem\":\"1 comprimido\",
  \"intervaloHoras\":12,
  \"dataInicio\":\"$INICIO_ANTIGO\",
  \"observacao\":\"Dar uma hora antes da refeição\"
}")

# A dose deste foi dada ontem à noite: o card abre em atraso.
ONTEM_20H="$(date -v-1d +%F 2>/dev/null || date -d '-1 day' +%F)T20:00:00"

registrar_dose() {
  curl -s -o /dev/null -X POST "$API/api/medicamentos/$1/doses" \
    -H "Authorization: Bearer $TOKEN_TUTOR" \
    -H 'Content-Type: application/json' -d "$2"
}

registrar_dose "$ID_CARDIO" "{\"dataHora\":\"$ONTEM_20H\"}"
echo "==> Vetmedin do Thor com a última dose ontem às 20h (aparece em atraso)"

# A amoxicilina teve a primeira dose agora: o card mostra o próximo horário
registrar_dose "$ID_AMOXI" '{}'
echo "==> Primeira dose da amoxicilina registrada pelo tutor"

echo
echo "==> Situação do tutor:"
curl -s "$API/api/tutores/$ID_TUTOR" -H "Authorization: Bearer $TOKEN_TUTOR" |
  python3 -c '
import sys, json
d = json.load(sys.stdin)
print("    pontos:", d["pontosTotais"], " |  moedas:", d["moedas"])
print("    nivel:", d["nivelDescricao"], " |  desconto:", str(d["descontoPercentual"]) + "%")
'
echo
echo "Pronto. Entre no app com tutor@animed.com.br ou doutor@animed.com.br (senha $SENHA)."
