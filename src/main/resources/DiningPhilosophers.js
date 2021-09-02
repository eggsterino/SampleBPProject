/* global bp */
function bthread(name, func) {
  bp.registerBThread(name, func)
}

function sync(stmt, data) {
  if (data)
    return bp.sync(stmt, data)
  return bp.sync(stmt)
}

function hot(isHot) {
  return bp.hot(isHot)
}

// ############ Basic behaviors ##############

const PHILOSOPHER_COUNT = 3

const Take = (i, side) => bp.Event('Take', {id: i, side: side})
const Put = (i, side) => bp.Event('Put', {id: i, side: side})
const AnyTake = i => [Take(i, "R"), Take((i % PHILOSOPHER_COUNT) + 1, "L")]
const AnyPut = i => [Put(i, "R"), Put((i % PHILOSOPHER_COUNT) + 1, "L")]

for (let c = 1; c <= PHILOSOPHER_COUNT; c++) {
  let i = c
  bthread('Stick ' + i, function () {
    while (true) {
      bp.sync({waitFor: AnyTake(i), block: AnyPut(i)});
      bp.sync({waitFor: AnyPut(i), block: AnyTake(i)});
    }
  })

  bthread('Philosopher ' + i, function () {
    while (true) {
      // Request to pick the right stick
      sync({request: [Take(i, 'R'), Take(i, 'L')]});
      sync({request: [Take(i, 'R'), Take(i, 'L')]});
      sync({request: [Put(i, 'R'), Put(i, 'L')]});
      sync({request: [Put(i, 'R'), Put(i, 'L')]});
    }
  })
}


// ############  Liveness requirements  ##############
for (let c = 1; c <= PHILOSOPHER_COUNT; c++) {
  let i = c

  // A taken stick will eventually be released
  bthread("[](take -> <>put)", function () {
    while (true) {
      sync({waitFor: AnyTake(i)});
      hot(true).sync({waitFor: AnyPut(i)})
    }
  })

  // A hungry philosopher will eventually eat
  /*bthread("NoStarvation", function () {
    while (true) {
      hot(true).sync({waitFor: Take(i, 'L')})
      sync({waitFor: Put(i, 'R')})
    }
  })*/
}


// ############  SOLUTION 1 - Semaphore  ##############
const TakeSemaphore = i => bp.Event('TakeSemaphore', i)
const ReleaseSemaphore = i => bp.Event('ReleaseSemaphore', i)
const AnyTakeSemaphore = bp.EventSet('AnyTakeSemaphore', function (e) {
  return e.name == 'TakeSemaphore'
})
const AnyReleaseSemaphore = bp.EventSet('AnyReleaseSemaphore', function (e) {
  return e.name == 'ReleaseSemaphore'
})

const AnyPhilosopherAction = i => bp.EventSet('AnyPhilosopherAction ' + i, function (e) {
  return ['Take', 'Put'].includes(String(e.name)) && e.data.id === i
})

bthread('Semaphore', function () {
  while (true) {
    sync({waitFor: AnyTakeSemaphore})
    sync({waitFor: AnyReleaseSemaphore, block: AnyTakeSemaphore})
  }
})

for (let c = 1; c <= PHILOSOPHER_COUNT; c++) {
  let i = c
  let es = AnyPhilosopherAction(i)
  bthread('Take semaphore ' + i, function () {
    while (true) {
      sync({request: TakeSemaphore(i), block: es})
      sync({waitFor: es})
      sync({waitFor: es})
      sync({request: ReleaseSemaphore(i), block: es})
    }
  })
}

