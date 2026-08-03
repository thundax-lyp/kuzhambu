function env(name, fallback) {
  var value = _getEnv(name);
  if (value === undefined || value === null || value === "") {
    return fallback;
  }
  return value;
}

function requireEnv(name) {
  var value = env(name, "");
  if (!value) {
    throw new Error("Missing required env: " + name);
  }
  return value;
}

function objectIdValue(name, value) {
  if (!/^[0-9a-fA-F]{24}$/.test(value)) {
    throw new Error("Invalid ObjectId env: " + name);
  }
  return ObjectId(value);
}

function configuredOrExistingObjectId(envName, collection, query) {
  var configured = env(envName, "");
  if (configured) {
    return objectIdValue(envName, configured);
  }
  var existing = collection.findOne(query, {_id: 1});
  return existing ? existing._id : ObjectId();
}

function boolEnv(name, fallback) {
  var value = env(name, fallback ? "true" : "false");
  return value === "true" || value === "1" || value === "yes";
}

function numberEnv(name, fallback) {
  var value = env(name, String(fallback));
  var parsed = Number(value);
  if (!isFinite(parsed)) {
    throw new Error("Invalid number env: " + name);
  }
  return parsed;
}

function waitForRootUser() {
  var deadline = Date.now() + numberEnv("FASTGPT_BOOTSTRAP_WAIT_MS", 120000);
  while (Date.now() < deadline) {
    var user = db.users.findOne({username: "root"});
    if (user) {
      return user;
    }
    sleep(2000);
  }
  throw new Error("FastGPT root user was not initialized before bootstrap timeout");
}

if (!boolEnv("FASTGPT_BOOTSTRAP_ENABLED", false)) {
  print("FastGPT bootstrap skipped");
  quit(0);
}

var rootUser = waitForRootUser();
var team = db.teams.findOne({ownerId: rootUser._id});
if (!team) {
  throw new Error("FastGPT root team is missing");
}
var teamMember = db.team_members.findOne({teamId: team._id, userId: rootUser._id});
if (!teamMember) {
  throw new Error("FastGPT root team member is missing");
}

var llmModel = requireEnv("FASTGPT_BOOTSTRAP_LLM_MODEL");
var llmName = env("FASTGPT_BOOTSTRAP_LLM_NAME", llmModel);
var llmBaseUrl = requireEnv("FASTGPT_BOOTSTRAP_LLM_BASE_URL");
var llmApiKey = requireEnv("FASTGPT_BOOTSTRAP_LLM_API_KEY");
var embeddingModel = requireEnv("FASTGPT_BOOTSTRAP_EMBEDDING_MODEL");
var embeddingName = env("FASTGPT_BOOTSTRAP_EMBEDDING_NAME", embeddingModel);
var embeddingBaseUrl = requireEnv("FASTGPT_BOOTSTRAP_EMBEDDING_BASE_URL");
var embeddingApiKey = requireEnv("FASTGPT_BOOTSTRAP_EMBEDDING_API_KEY");
var openApiKey = requireEnv("FASTGPT_KUZHAMBU_OPENAPI_KEY");
var datasetName = env("FASTGPT_KUZHAMBU_DATASET_NAME", "kuzhambu");
var appName = env("FASTGPT_KUZHAMBU_APP_NAME", "kuzhambu-qa");
var kuzhambuBaseUrl = env("FASTGPT_KUZHAMBU_BASE_URL", "http://fastgpt-app:3000");
var datasetId = configuredOrExistingObjectId("FASTGPT_KUZHAMBU_DATASET_ID", db.datasets, {name: datasetName});
var appId = configuredOrExistingObjectId("FASTGPT_KUZHAMBU_APP_ID", db.apps, {name: appName});

db.system_models.updateOne(
  {model: llmModel, "metadata.type": "llm"},
  {
    $set: {
      model: llmModel,
      metadata: {
        type: "llm",
        provider: "OpenAI",
        model: llmModel,
        name: llmName,
        maxContext: numberEnv("FASTGPT_BOOTSTRAP_LLM_MAX_CONTEXT", 16000),
        maxResponse: numberEnv("FASTGPT_BOOTSTRAP_LLM_MAX_RESPONSE", 4096),
        quoteMaxToken: numberEnv("FASTGPT_BOOTSTRAP_LLM_QUOTE_MAX_TOKEN", 12000),
        maxTemperature: numberEnv("FASTGPT_BOOTSTRAP_LLM_MAX_TEMPERATURE", 1.2),
        censor: false,
        vision: false,
        toolChoice: true,
        functionCall: false,
        defaultSystemChatPrompt: "",
        defaultConfig: {
          temperature: numberEnv("FASTGPT_BOOTSTRAP_LLM_TEMPERATURE", 0.2),
          max_tokens: numberEnv("FASTGPT_BOOTSTRAP_LLM_MAX_TOKENS", 4096)
        },
        isActive: true,
        isDefault: true,
        requestUrl: llmBaseUrl.replace(/\/$/, "") + "/chat/completions",
        requestAuth: llmApiKey,
        priceTiers: [{minInputTokens: 0, inputPrice: 0, outputPrice: 0}],
        showTopP: false,
        showStopSign: false,
        audio: false,
        video: false,
        reasoning: false,
        testMode: true
      }
    }
  },
  {upsert: true}
);

db.system_models.updateOne(
  {model: embeddingModel, "metadata.type": "embedding"},
  {
    $set: {
      model: embeddingModel,
      metadata: {
        type: "embedding",
        provider: "OpenAI",
        model: embeddingModel,
        name: embeddingName,
        charsPointsPrice: 0,
        inputPrice: "",
        outputPrice: "",
        priceTiers: "",
        isActive: true,
        isDefault: false,
        isDefaultDatasetTextModel: true,
        isDefaultDatasetImageModel: false,
        isDefaultChatTitleModel: false,
        requestUrl: embeddingBaseUrl.replace(/\/$/, "") + "/embeddings",
        requestAuth: embeddingApiKey,
        normalization: boolEnv("FASTGPT_BOOTSTRAP_EMBEDDING_NORMALIZATION", false),
        batchSize: numberEnv("FASTGPT_BOOTSTRAP_EMBEDDING_BATCH_SIZE", 10),
        defaultToken: numberEnv("FASTGPT_BOOTSTRAP_EMBEDDING_DEFAULT_TOKEN", 1024),
        maxToken: numberEnv("FASTGPT_BOOTSTRAP_EMBEDDING_MAX_TOKEN", 8192),
        vision: false,
        testMode: true
      }
    }
  },
  {upsert: true}
);

db.openapis.updateOne(
  {name: "kuzhambu-bootstrap"},
  {
    $set: {
      teamId: team._id,
      tmbId: teamMember._id,
      apiKey: openApiKey,
      tagIds: [],
      authProxy: false,
      name: "kuzhambu-bootstrap",
      limit: {maxUsagePoints: -1}
    },
    $setOnInsert: {
      usagePoints: 0,
      createTime: new Date()
    }
  },
  {upsert: true}
);

db.datasets.updateOne(
  {_id: datasetId},
  {
    $set: {
      parentId: null,
      teamId: team._id,
      tmbId: teamMember._id,
      type: "dataset",
      avatar: "core/dataset/commonDatasetColor",
      name: datasetName,
      vectorModel: embeddingModel,
      agentModel: llmModel,
      intro: "",
      inheritPermission: true,
      deleteTime: null,
      updateTime: new Date()
    },
    $setOnInsert: {
      createTime: new Date()
    }
  },
  {upsert: true}
);

db.apps.updateOne(
  {_id: appId},
  {
    $set: {
      teamId: team._id,
      tmbId: teamMember._id,
      name: appName,
      type: "advanced",
      avatar: "/icon/logo.svg",
      modules: [],
      edges: [],
      updateTime: new Date()
    },
    $setOnInsert: {
      createTime: new Date()
    }
  },
  {upsert: true}
);

print("KUZHAMBU_KNOWLEDGE_ENABLED=true");
print("KUZHAMBU_KNOWLEDGE_PROVIDER=fastgpt");
print("KUZHAMBU_KNOWLEDGE_FASTGPT_BASE_URL=" + kuzhambuBaseUrl);
print("KUZHAMBU_KNOWLEDGE_FASTGPT_API_KEY=" + openApiKey);
print("KUZHAMBU_KNOWLEDGE_FASTGPT_CHAT_API_KEY=" + openApiKey + "-" + appId.str);
print("KUZHAMBU_KNOWLEDGE_FASTGPT_APP_ID=" + appId.str);
print("KUZHAMBU_KNOWLEDGE_FASTGPT_APPID=" + appId.str);
print("KUZHAMBU_KNOWLEDGE_FASTGPT_KNOWLEDGE_BASE_ID=" + datasetId.str);
print("KUZHAMBU_KNOWLEDGE_FASTGPT_SYNC_MODE=auto");
print("KUZHAMBU_KNOWLEDGE_FASTGPT_TIMEOUT=" + env("FASTGPT_KUZHAMBU_TIMEOUT", "30s"));
