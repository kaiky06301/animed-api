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

echo -n "==> V3 na Mel: "
registrar_vacina "{
  \"nomeVacina\":\"V3 (tríplice felina)\",
  \"dataAplicacao\":\"2026-05-05\",
  \"dataProximaDose\":\"2027-05-05\",
  \"veterinarioResponsavel\":\"Dra. Helena Prado\",
  \"idPet\":$ID_MEL
}"
echo

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
