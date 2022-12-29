data = []
with open('./tests/test2.smt2', 'r',encoding='utf-8') as f:
  for line in f.readlines():
    line = line.strip()
    print(line)
    if 'assert' in line:
        data.append(line)
print(data)