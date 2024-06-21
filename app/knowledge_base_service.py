import numpy as np
import rdflib
import requests


def check_r2_score(r2, bot_url, resource_path, admin_chat_id, ticker, interval_choice):
    g = rdflib.Graph()
    rule_base = g.parse(f'{resource_path}/knowledge_base/rulebase.n3', format='text/n3')
    knowledge_base = g.parse(f'{resource_path}/knowledge_base/knowledgebase.n3', format='text/n3')

    successful_threshold_query = """
        SELECT ?threshold
        WHERE {
            element:R2S prop:hasSuccessfulThreshold ?threshold .
        }
    """

    threshold = None
    for row in g.query(successful_threshold_query):
        threshold = float(row.threshold)

    worst_value_query = """
        SELECT ?worstValue
        WHERE {
            element:R2S prop:hasTheWorstValue ?worstValue .
        }
    """

    worst_value = None
    for row in g.query(worst_value_query):
        worst_value = float(row.worstValue)

    if r2 < threshold:
        requests.post(bot_url, json={
            "chatId": admin_chat_id,
            "text": f"WARNING! R^2 Score appeared to be lower than defined successful threshold: ({threshold})\n"
                    f"Ticker: {ticker}\n"
                    f"Interval: {interval_choice}\n"
                    f"R^2 Score: {r2:.2f}"
        })
        if r2 < worst_value:
            knowledge_base.update(f"""
                DELETE {{
                    element:R2S prop:hasTheWorstValue ?value .
                }}
                INSERT {{
                    element:R2S prop:hasTheWorstValue "{r2}"^^xsd:float .
                }}
                WHERE {{
                    element:R2S prop:hasTheWorstValue ?value .
                }}
            """)
            knowledge_base.serialize(destination=f'{resource_path}/knowledge_base/knowledgebase.n3')


def generate_recommendation(df_stock, actual_price, next_interval_prediction, bot_url, resource_path, chat_id):
    g = rdflib.Graph()
    rule_base = g.parse(f'{resource_path}/knowledge_base/rulebase.n3', format='text/n3')
    knowledge_base = g.parse(f'{resource_path}/knowledge_base/knowledgebase.n3', format='text/n3')

    df_stock['Returns'] = np.log(df_stock['Close'] / df_stock['Close'].shift(1))
    volatility = df_stock['Returns'].std()
    diff = next_interval_prediction / actual_price - 1

    positive_price_change = diff > 0
    exceeds_volatility = abs(diff) > volatility

    recommendation_query = f"""
        SELECT ?description
        WHERE {{
            ?rec a class:Recommendation ;
                 prop:exceedsVolatility "{str(exceeds_volatility).lower()}"^^xsd:boolean ;
                 prop:description ?description .
            OPTIONAL {{ ?rec prop:hasPositivePriceChange ?hasPositivePriceChange . }}
            FILTER (!BOUND(?hasPositivePriceChange) || ?hasPositivePriceChange = "{str(positive_price_change).lower()}"^^xsd:boolean)
        }}
        """

    recommendation = None
    for row in g.query(recommendation_query):
        recommendation = str(row.description)

    requests.post(bot_url, json={
        "chatId": chat_id,
        "text": recommendation
    })
