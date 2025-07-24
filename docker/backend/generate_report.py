#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import json
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import re
import io
import base64
from matplotlib.ticker import MaxNLocator
from datetime import datetime
import math

def clean_size_str(size_str):
    """Converts size strings to numeric KB for comparison"""
    if isinstance(size_str, str):
        if 'MB' in size_str:
            return float(re.sub(r'[^\d.,]', '', size_str.replace(',', '.'))) * 1024
        elif 'GB' in size_str:
            return float(re.sub(r'[^\d.,]', '', size_str.replace(',', '.'))) * 1024 * 1024
        elif 'kB' in size_str:
            return float(re.sub(r'[^\d.,]', '', size_str.replace(',', '.')))
        else:
            return float(re.sub(r'[^\d.,]', '', size_str.replace(',', '.')))
    return size_str

def normalize_data_structure(benchmark_data):
    """
    Normalize different JSON structures to a common format
    Supports both nested format with 'results' and direct format
    """
    normalized_data = {}

    # First pass: collect all query names for all DBs if using direct format
    all_direct_query_names = set()
    for db_name, db_data in benchmark_data.items():
        if "results" not in db_data and "query" in db_data and "avgExecutionTime" in db_data:
            # Try to extract a query name as before
            query_text = db_data["query"]
            query_name = "QUERY"
            sql_lowercase = query_text.lower()
            if "select" in sql_lowercase:
                match = re.search(r'select\s+(\w+)', sql_lowercase)
                if match:
                    field = match.group(1)
                    if field != '*':
                        query_name = field.upper()
                from_match = re.search(r'from\s+(\w+)', sql_lowercase)
                if from_match:
                    table = from_match.group(1)
                    if "where" in sql_lowercase:
                        query_name = f"FILTER"
                    elif "join" in sql_lowercase:
                        query_name = f"JOIN"
                    elif "group by" in sql_lowercase:
                        query_name = f"GROUP"
                    elif "order by" in sql_lowercase:
                        query_name = f"ORDER"
            all_direct_query_names.add(query_name)

    # If there is only one unique query name in direct format, force it to "Query"
    if len(all_direct_query_names) == 1:
        force_single_query_name = True
    else:
        force_single_query_name = False

    for db_name, db_data in benchmark_data.items():
        normalized_db = {}

        # Case 1: Nested format with 'results' key
        if "results" in db_data:
            normalized_db["results"] = db_data["results"]
            if "indexInfo" in db_data:
                normalized_db["indexInfo"] = db_data["indexInfo"]
        # Case 2: Direct format with query data at root level
        elif "query" in db_data and "avgExecutionTime" in db_data:
            query_text = db_data["query"]
            query_name = "QUERY"
            sql_lowercase = query_text.lower()
            if "select" in sql_lowercase:
                match = re.search(r'select\s+(\w+)', sql_lowercase)
                if match:
                    field = match.group(1)
                    if field != '*':
                        query_name = field.upper()
                from_match = re.search(r'from\s+(\w+)', sql_lowercase)
                if from_match:
                    table = from_match.group(1)
                    if "where" in sql_lowercase:
                        query_name = f"FILTER"
                    elif "join" in sql_lowercase:
                        query_name = f"JOIN"
                    elif "group by" in sql_lowercase:
                        query_name = f"GROUP"
                    elif "order by" in sql_lowercase:
                        query_name = f"ORDER"
            # If only one query, force name to "Query"
            if force_single_query_name:
                query_name = "Query"
            normalized_db["results"] = {
                query_name: {
                    "query": db_data["query"],
                    "avgExecutionTime": db_data.get("avgExecutionTime", 0),
                    "queryPerSecond": db_data.get("queryPerSecond", 0),
                    "explainPlan": db_data.get("explainPlan", ""),
                    "initialConnectionTime": db_data.get("initialConnectionTime", 0),
                    "standardDeviation": db_data.get("standardDeviation", 0),
                    "variance": db_data.get("variance", 0),
                    "percentile95": db_data.get("percentile95", 0)
                }
            }
            if "cacheInfo" in db_data:
                normalized_db["results"][query_name]["cacheInfo"] = db_data["cacheInfo"]
            if "indexInfo" in db_data:
                normalized_db["indexInfo"] = db_data["indexInfo"]
        normalized_data[db_name] = normalized_db
    return normalized_data

def generate_html_report(benchmark_data):
    """Generates a complete HTML report with embedded charts"""
    # Normalize the data structure
    normalized_data = normalize_data_structure(benchmark_data)
    
    databases = list(normalized_data.keys())
    report_parts = []
    chart_base64_strings = []
    
    # Define distinct color palette for databases
    db_colors = {
        databases[i]: color for i, color in enumerate([
            "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", 
            "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf"
        ][:len(databases)])
    }
    
    # CSS style for a clean report
    css = """
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6;}
        h1, h2, h3, h4 { color: #333; margin-top: 30px;}
        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);}
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; font-weight: bold;}
        tr:nth-child(even) { background-color: #f9f9f9; }
        .chart-container { margin: 30px 0; page-break-inside: avoid; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 10px; background-color: white;}
        .section { margin-bottom: 40px; page-break-inside: avoid; background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);}
        .subsection { margin-top: 25px; margin-bottom: 25px;}
        @media print { .page-break { page-break-before: always; } body { margin: 15mm;} }
        .summary-box { background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 5px; padding: 15px; margin-bottom: 20px;}
        .highlight { background-color: #fffacd; font-weight: bold;}
        pre { background-color: #f5f5f5; padding: 10px; border-radius: 5px; overflow-x: auto; font-family: monospace; white-space: pre-wrap; }
        .db-name { font-weight: bold; color: #2c3e50;}
        .query-section { margin-bottom: 30px; border-left: 4px solid #3498db; padding-left: 15px;}
        .connection-info { background-color: #f8f9fa; padding: 10px 15px; border-radius: 5px; margin: 15px 0; display: inline-block;}
        .top-summary { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 15px; margin-bottom: 30px;}
        .summary-card { flex: 1; min-width: 250px; background-color: #f8f9fa; border-radius: 5px; padding: 15px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);}
        .summary-card h4 { margin-top: 0; margin-bottom: 10px; border-bottom: 1px solid #ddd; padding-bottom: 5px;}
        .best-time { color: #27ae60; font-weight: bold;}
        .best-qps { color: #3498db; font-weight: bold;}
        .db-query-section { margin: 20px 0; padding: 15px; background-color: #f9f9f9; border-radius: 5px; border-left: 4px solid #2980b9;}
        .sql-statements-section, .execution-plans-section { margin: 20px 0; padding: 10px; border-top: 1px solid #eee;}
        .charts-grid { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 20px; margin-top: 30px;}
        .charts-grid-db { display: flex; flex-direction: column; gap: 30px; margin-top: 30px;}
        .chart-item-full { flex: 0 0 100%; margin-bottom: 20px; background: white; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);}
        .chart-section-title { margin-top: 40px; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #e0e0e0;}
        @media (max-width: 1200px) { .chart-item { flex: 0 0 calc(50% - 10px); } }
        @media (max-width: 768px) { .chart-item, .chart-item-full { flex: 0 0 100%; } }
        .large-query-chart { min-height: 400px;}
        .large-query-chart img { max-height: 100%;}
    </style>
    """
    
    # Current date and time
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    report_parts.append(f"""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>Database Benchmark Report</title>
        {css}
    </head>
    <body>
        <h1>Database Benchmark Results Report</h1>
        <div class="summary-box">
            <p><strong>Generated:</strong> {now}</p>
            <p><strong>Databases Benchmarked:</strong> {", ".join(databases)}</p>
        </div>
    """)
    
    # Summary of database connection times
    if len(databases) > 0:
        report_parts.append("""
        <div class="top-summary">
        """)
        for db_name in databases:
            connection_time = "N/A"
            if "results" in normalized_data[db_name] and normalized_data[db_name]["results"]:
                first_query = next(iter(normalized_data[db_name]["results"].values()))
                if "initialConnectionTime" in first_query:
                    connection_time = f"{first_query['initialConnectionTime']} ms"
            report_parts.append(f"""
            <div class="summary-card">
                <h4>{db_name}</h4>
                <p><strong>Initial Connection Time:</strong> {connection_time}</p>
            </div>
            """)
        report_parts.append("</div>")
    
    # SECTION 1: Query Execution Results
    report_parts.append("""
    <div class="section">
        <h2>Query Execution Results</h2>
    """)
    query_results = []
    cache_results = []
    query_names = set()
    for db_name in databases:
        if "results" in normalized_data[db_name]:
            for query_name, query_data in normalized_data[db_name]["results"].items():
                query_names.add(query_name)
                query_results.append({
                    "Database": db_name,
                    "Query": query_name,
                    "Avg. Execution Time (ms)": query_data["avgExecutionTime"],
                    "Queries/Second": query_data["queryPerSecond"],
                    "Standard Deviation": query_data.get("standardDeviation", "N/A"),
                    "Variance": query_data.get("variance", "N/A"),
                    "95th Percentile": query_data.get("percentile95", "N/A")
                })
                if "cacheInfo" in query_data:
                    cache_results.append({
                        "Database": db_name,
                        "Query": query_name,
                        "Cache Hits": query_data["cacheInfo"]["hits"],
                        "Cache Misses": query_data["cacheInfo"]["misses"],
                        "Cache Hit Ratio (%)": query_data["cacheInfo"]["hitsRatio"]
                    })
    query_results_df = pd.DataFrame(query_results).sort_values(by=["Query", "Database"])
    if not query_results_df.empty:
        report_parts.append("""
        <h3>Performance Metrics</h3>
        <table>
            <tr>
                <th>Query</th>
                <th>Database</th>
                <th>Avg. Execution Time (ms)</th>
                <th>Queries/Second</th>
                <th>Standard Deviation</th>
                <th>Variance</th>
                <th>95th Percentile</th>
            </tr>
        """)
        current_query = None
        for _, row in query_results_df.iterrows():
            row_class = ""
            if current_query != row["Query"]:
                row_class = ' style="border-top: 2px solid #3498db;"'
                current_query = row["Query"]
            report_parts.append(f"""
            <tr{row_class}>
                <td>{row["Query"]}</td>
                <td>{row["Database"]}</td>
                <td>{row["Avg. Execution Time (ms)"]}</td>
                <td>{row["Queries/Second"]}</td>
                <td>{row["Standard Deviation"]}</td>
                <td>{row["Variance"]}</td>
                <td>{row["95th Percentile"]}</td>
            </tr>
            """)
        report_parts.append("</table>")
    if cache_results:
        cache_df = pd.DataFrame(cache_results).sort_values(by=["Query", "Database"])
        report_parts.append("""
        <h3>Cache Information</h3>
        <table>
            <tr>
                <th>Query</th>
                <th>Database</th>
                <th>Cache Hits</th>
                <th>Cache Misses</th>
                <th>Cache Hit Ratio (%)</th>
            </tr>
        """)
        current_query = None
        for _, row in cache_df.iterrows():
            row_class = ""
            if current_query != row["Query"]:
                row_class = ' style="border-top: 2px solid #3498db;"'
                current_query = row["Query"]
            report_parts.append(f"""
            <tr{row_class}>
                <td>{row["Query"]}</td>
                <td>{row["Database"]}</td>
                <td>{row["Cache Hits"]}</td>
                <td>{row["Cache Misses"]}</td>
                <td>{row["Cache Hit Ratio (%)"]}</td>
            </tr>
            """)
        report_parts.append("</table>")
    report_parts.append("</div>")  # End of Query Execution Results section
    
    # SECTION 2: Performance Charts (remains only DB-specific, and switched to horizontal)
    report_parts.append("""
    <div class="section">
        <h2>Performance Charts</h2>
        <h3>Comparison Charts</h3>
    """)
    if not query_results_df.empty:
        # Chart 1: Execution Time Comparison by Query (horizontal)
        plt.figure(figsize=(14, 8))
        palette = {db: db_colors[db] for db in query_results_df["Database"].unique()}
        ax = sns.barplot(y="Query", x="Avg. Execution Time (ms)", hue="Database", 
                         data=query_results_df, palette=palette, orient='h')
        plt.title("Query Execution Time Comparison", fontsize=15)
        plt.ylabel("Query", fontsize=12)
        plt.xlabel("Avg. Execution Time (ms)", fontsize=12)
        plt.legend(title="Database")
        for p in ax.patches:
            width = p.get_width()
            if not pd.isna(width):
                ax.text(width + 0.1, p.get_y() + p.get_height()/2., f'{width:.1f}', va="center", fontsize=9)
        plt.tight_layout()
        buffer = io.BytesIO()
        plt.savefig(buffer, format="png", dpi=120)
        buffer.seek(0)
        image_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")
        chart_base64_strings.append(image_base64)
        plt.close()
        report_parts.append(f"""
        <div class="chart-container">
            <h4>Query Execution Time Comparison</h4>
            <img src="data:image/png;base64,{image_base64}" width="100%" />
        </div>
        """)
        
        # Chart 2: Queries Per Second Comparison (horizontal)
        plt.figure(figsize=(14, 8))
        ax = sns.barplot(y="Query", x="Queries/Second", hue="Database", 
                        data=query_results_df, palette=palette, orient='h')
        plt.title("Queries Per Second Comparison", fontsize=15)
        plt.ylabel("Query", fontsize=12)
        plt.xlabel("Queries/Second (higher is better)", fontsize=12)
        plt.legend(title="Database")
        for p in ax.patches:
            width = p.get_width()
            if not pd.isna(width):
                ax.text(width + 0.1, p.get_y() + p.get_height()/2., f'{width:.1f}', va="center", fontsize=9)
        plt.tight_layout()
        buffer = io.BytesIO()
        plt.savefig(buffer, format="png", dpi=120)
        buffer.seek(0)
        image_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")
        chart_base64_strings.append(image_base64)
        plt.close()
        report_parts.append(f"""
        <div class="chart-container">
            <h4>Queries Per Second Comparison</h4>
            <img src="data:image/png;base64,{image_base64}" width="100%" />
        </div>
        """)
        
        if len(query_names) > 1:
            report_parts.append("""
            <h3 class="chart-section-title">Database-Specific Performance Charts</h3>
            <p>Each chart shows performance across queries for a specific database.</p>
            <div class="charts-grid-db">
            """)
            for db_name in databases:
                if "results" in normalized_data[db_name]:
                    db_data = query_results_df[query_results_df["Database"] == db_name]
                    if not db_data.empty:
                        # Horizontal bars for per-db charts
                        plt.figure(figsize=(14, 6))
                        ax = sns.barplot(y="Query", x="Avg. Execution Time (ms)", data=db_data, color=db_colors[db_name], orient='h')
                        plt.title(f"{db_name} - Execution Times", fontsize=14)
                        plt.ylabel("Query", fontsize=11)
                        plt.xlabel("Avg. Execution Time (ms)", fontsize=11)
                        for p in ax.patches:
                            width = p.get_width()
                            if not pd.isna(width):
                                ax.text(width + 0.1, p.get_y() + p.get_height()/2., f'{width:.1f}', va="center", fontsize=9)
                        plt.tight_layout()
                        buffer = io.BytesIO()
                        plt.savefig(buffer, format="png", dpi=100)
                        buffer.seek(0)
                        image_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")
                        chart_base64_strings.append(image_base64)
                        plt.close()
                        report_parts.append(f"""
                        <div class="chart-item-full">
                            <h4>{db_name} Execution Times</h4>
                            <img src="data:image/png;base64,{image_base64}" width="100%" />
                        </div>
                        """)
            report_parts.append("</div>")
    report_parts.append("</div>")
    
    # SECTION 3: Query Execution Plans
    report_parts.append("""
    <div class="section">
        <h2>Query Execution Plans</h2>
    """)
    for query_name in sorted(query_names):
        report_parts.append(f"""
        <div class="query-section">
            <h3>Query: {query_name}</h3>
            <div class="sql-statements-section">
                <h4>SQL Statements</h4>
        """)
        for db_name in databases:
            if "results" in normalized_data[db_name] and query_name in normalized_data[db_name]["results"]:
                query_data = normalized_data[db_name]["results"][query_name]
                report_parts.append(f"""
                <div class="db-query-section">
                    <h5>{db_name} SQL</h5>
                    <pre>{query_data["query"]}</pre>
                </div>
                """)
        report_parts.append("""
            </div> <!-- End of SQL statements section -->
            <div class="execution-plans-section">
                <h4>Execution Plans</h4>
        """)
        for db_name in databases:
            if "results" in normalized_data[db_name] and query_name in normalized_data[db_name]["results"]:
                query_data = normalized_data[db_name]["results"][query_name]
                report_parts.append(f"""
                <div class="db-query-section">
                    <h5>{db_name} Execution Plan</h5>
                    <pre>{query_data["explainPlan"]}</pre>
                </div>
                """)
        report_parts.append("""
            </div> <!-- End of execution plans section -->
        """)
        # Horizontal bar chart for this query
        query_data_df = query_results_df[query_results_df["Query"] == query_name]
        n_databases = len(query_data_df["Database"])
        if not query_data_df.empty:
            plt.figure(figsize=(12, max(2, 0.7 * n_databases)))
            bars = plt.barh(
                query_data_df["Database"],
                query_data_df["Avg. Execution Time (ms)"],
                color=[db_colors[db] for db in query_data_df["Database"]],
                height=0.2
            )
            plt.title(f"Avg. Execution Time per Database for query: {query_name}", fontsize=15)
            plt.ylabel("Database", fontsize=13)
            plt.xlabel("Avg. Execution Time (ms)", fontsize=13)
            plt.yticks(fontsize=12)
            plt.xticks(fontsize=12)
            for bar in bars:
                width = bar.get_width()
                plt.text(width + 0.1, bar.get_y() + bar.get_height()/2., f'{width:.1f}', va='center', fontsize=12)
            plt.tight_layout()
            buffer = io.BytesIO()
            plt.savefig(buffer, format="png", dpi=120)
            buffer.seek(0)
            image_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")
            plt.close()
            report_parts.append(f"""
                <div class="chart-container large-query-chart" style="margin-bottom:20px;">
                    <h4>Avg. Execution Time Comparison</h4>
                    <img src="data:image/png;base64,{image_base64}" width="100%" height="auto" />
                </div>
            """)
        report_parts.append("""
        </div> <!-- End of this query section -->
        """)
    report_parts.append("</div>")
    
    # SECTION 4: Index Information
    report_parts.append("""
    <div class="section page-break">
        <h2>Index Information</h2>
    """)
    for db_name in databases:
        report_parts.append(f"""
        <div class="subsection">
            <h3>{db_name} Index Information</h3>
            <table>
                <tr>
                    <th>Index Name</th>
                    <th>Table</th>
                    <th>Index Size</th>
                    <th>Table Size</th>
                    <th>Size Ratio (%)</th>
                </tr>
        """)
        if "indexInfo" in normalized_data[db_name]:
            for idx in normalized_data[db_name]["indexInfo"]:
                report_parts.append(f"""
                <tr>
                    <td>{idx["indexName"]}</td>
                    <td>{idx["table"]}</td>
                    <td>{idx["indexSize"]}</td>
                    <td>{idx["tableSize"]}</td>
                    <td>{idx["sizeRatio"]}</td>
                </tr>
                """)
        report_parts.append("</table>")
        # Index Size vs Table Size (horizontal)
        if "indexInfo" in normalized_data[db_name]:
            df_index = pd.DataFrame(normalized_data[db_name]["indexInfo"])
            plt.figure(figsize=(12, 8))
            sns.set_style("whitegrid")
            df_index["indexSize_kb"] = df_index["indexSize"].apply(clean_size_str)
            df_index["tableSize_kb"] = df_index["tableSize"].apply(clean_size_str)
            df_index_sorted = df_index.sort_values(by="indexSize_kb", ascending=False).head(10)
            bar_width = 0.4
            index = range(len(df_index_sorted))
            # Index size
            plt.barh([i - bar_width/2 for i in index], df_index_sorted["indexSize_kb"]/1024, 
                    height=bar_width, label="Index Size (MB)", color="royalblue")
            plt.barh([i + bar_width/2 for i in index], df_index_sorted["tableSize_kb"]/1024, 
                    height=bar_width, label="Table Size (MB)", color="lightcoral")
            plt.ylabel("Index Name")
            plt.xlabel("Size (MB)")
            plt.title(f"{db_name} - Top 10 Index vs Table Size")
            plt.yticks(index, df_index_sorted["indexName"])
            plt.legend()
            plt.tight_layout()
            buffer = io.BytesIO()
            plt.savefig(buffer, format="png", dpi=120)
            buffer.seek(0)
            image_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")
            chart_base64_strings.append(image_base64)
            plt.close()
            report_parts.append(f"""
            <div class="chart-container">
                <img src="data:image/png;base64,{image_base64}" width="100%" />
            </div>
            """)
            # Size Ratio (horizontal)
            plt.figure(figsize=(12, 6))
            df_ratio = df_index.sort_values(by="sizeRatio", ascending=False).head(10)
            sns.barplot(y="indexName", x="sizeRatio", hue="indexName", data=df_ratio, palette="viridis", orient='h', legend=False)
            plt.title(f"{db_name} - Top 10 Size Ratios (Index Size / Table Size)")
            plt.ylabel("Index Name")
            plt.xlabel("Size Ratio (%)")
            plt.tight_layout()
            buffer = io.BytesIO()
            plt.savefig(buffer, format="png", dpi=120)
            buffer.seek(0)
            image_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")
            chart_base64_strings.append(image_base64)
            plt.close()
            report_parts.append(f"""
            <div class="chart-container">
                <img src="data:image/png;base64,{image_base64}" width="100%" />
            </div>
            """)
        report_parts.append("</div>")
    report_parts.append("</div>")
    report_parts.append("""
    </body>
    </html>
    """)
    full_report = "\n".join(report_parts)
    return full_report

def main(input_file, output_file):
    print(f"Reading input file: {input_file}")
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            benchmark_data = json.load(f)
    except Exception as e:
        print(f"Error reading JSON file: {e}")
        sys.exit(1)
    print(f"Generating HTML report...")
    try:
        html_content = generate_html_report(benchmark_data)
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(html_content)
        print(f"Report generated successfully at: {output_file}")
    except Exception as e:
        print(f"Error generating report: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python generate_report.py input_file output_file")
        sys.exit(1)
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    main(input_file, output_file)